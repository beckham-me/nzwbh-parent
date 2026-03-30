package com.xs.nzwbh.pest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.CropDiseasesAndPestsDto;
import com.xs.nzwbh.model.entity.CropDiseasesAndPests;
import com.xs.nzwbh.model.esentity.CropDiseasesAndPestsDocument;
import com.xs.nzwbh.model.mgrvo.DiseasesAndPestsVo;
import com.xs.nzwbh.model.vo.CropDiseasesAndPestsVo;
import com.xs.nzwbh.model.vo.SearchVo;
import com.xs.nzwbh.pest.es.repository.CropDiseasesAndPestsRepository;
import com.xs.nzwbh.pest.mapper.CropPestMapper;
import com.xs.nzwbh.pest.service.CropPestService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CropPestServiceImpl extends ServiceImpl<CropPestMapper, CropDiseasesAndPests> implements CropPestService {

    @Autowired
    private CropPestMapper cropPestMapper;

    @Autowired
    private CropDiseasesAndPestsRepository esRepository;

    @Autowired
    private RBloomFilter<String> pestBloomFilter;

    // ========== 原有业务方法（改造后优先使用 ES） ==========

    @Override
    public List<CropDiseasesAndPestsVo> getCropDiseasesInfo(Long cropId) {
        // 优先从 ES 查询病害数据
        List<CropDiseasesAndPestsDocument> documents = esRepository.findByCropIdAndType(cropId, "病害");
        if (!documents.isEmpty()) {
            return documents.stream()
                    .map(this::convertToVo)
                    .collect(Collectors.toList());
        }

        // 降级到 MySQL
        List<CropDiseasesAndPests> cropDiseasesAndPestsList = cropPestMapper.selectByCropId(cropId);
        if (cropDiseasesAndPestsList == null || cropDiseasesAndPestsList.isEmpty()) {
            throw new RuntimeException("未查询到相关数据");
        }

        List<CropDiseasesAndPestsVo> cropDiseasesVoList = new ArrayList<>();
        for (CropDiseasesAndPests entity : cropDiseasesAndPestsList) {
            if ("病害".equals(entity.getType())) {
                CropDiseasesAndPestsVo vo = new CropDiseasesAndPestsVo();
                BeanUtils.copyProperties(entity, vo);
                cropDiseasesVoList.add(vo);
            }
        }
        return cropDiseasesVoList;
    }

    @Override
    public List<CropDiseasesAndPestsVo> getCropPestsInfo(Long cropId) {
        // 优先从 ES 查询虫害数据
        List<CropDiseasesAndPestsDocument> documents = esRepository.findByCropIdAndType(cropId, "虫害");
        if (!documents.isEmpty()) {
            return documents.stream()
                    .map(this::convertToVo)
                    .collect(Collectors.toList());
        }

        // 降级到 MySQL
        List<CropDiseasesAndPests> cropDiseasesAndPestsList = cropPestMapper.selectByCropId(cropId);
        if (cropDiseasesAndPestsList == null || cropDiseasesAndPestsList.isEmpty()) {
            throw new RuntimeException("未查询到相关数据");
        }

        List<CropDiseasesAndPestsVo> cropPestsVoList = new ArrayList<>();
        for (CropDiseasesAndPests entity : cropDiseasesAndPestsList) {
            if ("虫害".equals(entity.getType())) {
                CropDiseasesAndPestsVo vo = new CropDiseasesAndPestsVo();
                BeanUtils.copyProperties(entity, vo);
                cropPestsVoList.add(vo);
            }
        }



        return cropPestsVoList;
    }

    @Override
    public CropDiseasesAndPestsVo getCropDiseasesAndPestsInfo(Long id) {
        // 1. 布隆过滤器快速判断不存在
        if (!pestBloomFilter.contains(String.valueOf(id))) {
            log.warn("布隆过滤器判断 ID {} 不存在", id);
            throw new RuntimeException("未查询到相关数据");
        }

        // 2. 优先从 ES 查询
        Optional<CropDiseasesAndPestsDocument> opt = esRepository.findById(id);
        if (opt.isPresent()) {
            CropDiseasesAndPestsDocument doc = opt.get();
            // 如果已逻辑删除，返回空（布隆过滤器可能存在误判）
            if (doc.getIsDeleted() != null && doc.getIsDeleted() == 1) {
                throw new RuntimeException("数据已删除");
            }
            return convertToVo(doc);
        }

        // 3. 降级到 MySQL（可能刚插入，同步延迟）
        CropDiseasesAndPests entity = cropPestMapper.selectById(id);
        if (entity == null || entity.getIsDeleted() == 1) {
            throw new RuntimeException("未查询到相关数据");
        }

        // 4. 同步到 ES 并加入布隆过滤器
        CropDiseasesAndPestsDocument doc = convertToDocument(entity);
        esRepository.save(doc);
        pestBloomFilter.add(String.valueOf(id));

        CropDiseasesAndPestsVo vo = new CropDiseasesAndPestsVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<CropDiseasesAndPestsVo> getCommonCropDiseasesAndPestsInfo() {
        // 从 MySQL 查询常见病虫害（按更新时间排序，取前 3 条）
        LambdaQueryWrapper<CropDiseasesAndPests> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CropDiseasesAndPests::getUpdateTime).last("limit 3");
        List<CropDiseasesAndPests> common = cropPestMapper.selectList(queryWrapper);
        if (common == null || common.isEmpty()) {
            throw new RuntimeException("未查询到相关数据");
        }

        List<CropDiseasesAndPestsVo> commonVo = new ArrayList<>();
        for (CropDiseasesAndPests entity : common) {
            CropDiseasesAndPestsVo vo = new CropDiseasesAndPestsVo();
            BeanUtils.copyProperties(entity, vo);
            commonVo.add(vo);
        }
        return commonVo;
    }

    @Override
    public SearchVo findMostSimilarName(String keyword) {
        // 优先从 ES 模糊匹配，取前20条（可根据实际情况调整）
        List<CropDiseasesAndPestsDocument> docs = esRepository.findByNameContaining(keyword);
        if (!docs.isEmpty()) {
            // 计算相似度并排序，取相似度最高的前10条
            List<CropDiseasesAndPestsDocument> sorted = docs.stream()
                    .sorted((a, b) -> Double.compare(
                            calculateSimilarity(keyword, b.getName()),
                            calculateSimilarity(keyword, a.getName())
                    ))
                    .limit(10)   // 最多取10条
                    .collect(Collectors.toList());

            // 返回最相似的一条（如果需要返回前10条，可改为返回列表）
            CropDiseasesAndPestsDocument mostSimilar = sorted.get(0);
            SearchVo vo = new SearchVo();
            BeanUtils.copyProperties(mostSimilar, vo);
            vo.setType(mostSimilar.getType()); // 病害或虫害
            return vo;
        }

        // 降级到 MySQL
        LambdaQueryWrapper<CropDiseasesAndPests> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CropDiseasesAndPests::getName, keyword)
                .orderByDesc(CropDiseasesAndPests::getName).last("LIMIT 1");
        CropDiseasesAndPests entity = cropPestMapper.selectOne(queryWrapper);
        if (entity == null) {
            throw new RuntimeException("未查询到相关数据");
        }
        SearchVo vo = new SearchVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setType(entity.getType());
        return vo;
    }

    @Override
    public List<String> findSimilarDiseaseAndPestNames(String keyword) {
        // 优先从 ES 查询
        List<CropDiseasesAndPestsDocument> docs = esRepository.findByNameContaining(keyword);
        if (!docs.isEmpty()) {
            return docs.stream()
                    .limit(4)
                    .map(CropDiseasesAndPestsDocument::getName)
                    .collect(Collectors.toList());
        }

        // 降级到 MySQL
        LambdaQueryWrapper<CropDiseasesAndPests> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CropDiseasesAndPests::getName, keyword)
                .orderByDesc(CropDiseasesAndPests::getName).last("LIMIT 4");
        List<CropDiseasesAndPests> similarNames = cropPestMapper.selectList(queryWrapper);
        return similarNames.stream()
                .map(CropDiseasesAndPests::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getrPestNames(Long cropId) {
        // 优先从 ES 查询
        List<CropDiseasesAndPestsDocument> docs = esRepository.findByCropIdAndType(cropId, "虫害");
        if (!docs.isEmpty()) {
            return docs.stream()
                    .map(CropDiseasesAndPestsDocument::getName)
                    .collect(Collectors.toList());
        }

        // 降级到 MySQL
        LambdaQueryWrapper<CropDiseasesAndPests> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CropDiseasesAndPests::getCropId, cropId);
        List<CropDiseasesAndPests> list = cropPestMapper.selectList(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return list.stream()
                    .map(CropDiseasesAndPests::getName)
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public Result<Page<DiseasesAndPestsVo>> getCropDiseasesAndPests(Integer page, Integer size, String keyword) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            org.springframework.data.domain.Page<CropDiseasesAndPestsDocument> esPage;

            if (keyword != null && !keyword.isEmpty()) {
                esPage = esRepository.findByNameContaining(keyword, pageable);
            } else {
                esPage = esRepository.findAll(pageable); // 这里返回的就是 Page
            }

            // 转换为 MyBatis-Plus 分页对象
            Page<DiseasesAndPestsVo> resultPage = new Page<>(page, size);
            resultPage.setTotal(esPage.getTotalElements());
            resultPage.setRecords(esPage.getContent().stream()
                    .map(this::convertDocumentToVo)
                    .collect(Collectors.toList()));
            return Result.ok(resultPage);
        } catch (Exception e) {
            // 3. ES 查询失败，降级到 MySQL
            log.warn("ES 分页查询失败，降级到 MySQL", e);
            Page<CropDiseasesAndPests> pestsPage = new Page<>(page, size);
            QueryWrapper<CropDiseasesAndPests> queryWrapper = new QueryWrapper<>();
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("name", keyword);
            }
            Page<CropDiseasesAndPests> pestsPageResult = cropPestMapper.selectPage(pestsPage, queryWrapper);

            Page<DiseasesAndPestsVo> pestVoPage = new Page<>();
            pestVoPage.setCurrent(pestsPageResult.getCurrent());
            pestVoPage.setSize(pestsPageResult.getSize());
            pestVoPage.setTotal(pestsPageResult.getTotal());
            pestVoPage.setPages(pestsPageResult.getPages());
            pestVoPage.setRecords(pestsPageResult.getRecords().stream()
                    .map(this::convertEntityToVo)
                    .collect(Collectors.toList()));
            return Result.ok(pestVoPage);
        }
    }

    // ========== 写操作（同步 ES） ==========

    @Override
    @Transactional
    public Result<Void> addCropDiseaseAndPest(CropDiseasesAndPestsDto dto) {
        CropDiseasesAndPests entity = new CropDiseasesAndPests();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setIsDeleted(0);
        boolean success = cropPestMapper.insert(entity) > 0;

        if (success) {
            // 同步到 ES
            CropDiseasesAndPestsDocument doc = convertToDocument(entity);
            esRepository.save(doc);
            // 加入布隆过滤器
            pestBloomFilter.add(String.valueOf(entity.getId()));
            log.info("新增病虫害成功，ID={}", entity.getId());
        }
        return success ? Result.ok() : Result.fail();
    }

    @Override
    @Transactional
    public Result<Void> updateCropDiseaseAndPest(CropDiseasesAndPestsDto dto) {
        if (dto.getId() == null) {
            log.error("病虫害 ID 不能为空");
            return Result.fail();
        }

        CropDiseasesAndPests entity = cropPestMapper.selectById(dto.getId());
        if (entity == null) {
            log.error("病虫害不存在，ID={}", dto.getId());
            return Result.fail();
        }

        // 只更新允许修改的字段
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setImage(dto.getImage());
        entity.setDescription(dto.getDescription());
        entity.setCause(dto.getCause());
        entity.setSolution(dto.getSolution());
        entity.setUpdateTime(new Date());

        boolean success = cropPestMapper.updateById(entity) > 0;
        if (success) {
            // 同步到 ES
            CropDiseasesAndPestsDocument doc = convertToDocument(entity);
            esRepository.save(doc);
            log.info("更新病虫害成功，ID={}", entity.getId());
        }
        return success ? Result.ok() : Result.fail();
    }

    @Override
    @Transactional
    public Result<Void> deleteCropDiseaseAndPest(Long id) {
        // 逻辑删除
        CropDiseasesAndPests entity = cropPestMapper.selectById(id);
        if (entity == null) {
            log.error("病虫害不存在，ID={}", id);
            return Result.fail();
        }
        entity.setIsDeleted(1);
        entity.setUpdateTime(new Date());
        boolean success = cropPestMapper.updateById(entity) > 0;

        if (success) {
            // 同步到 ES：更新 isDeleted 字段，或直接删除文档
            CropDiseasesAndPestsDocument doc = convertToDocument(entity);
            esRepository.save(doc);
            log.info("逻辑删除病虫害成功，ID={}", id);
        }
        return success ? Result.ok() : Result.fail();
    }

    // ========== 私有辅助方法 ==========

    private CropDiseasesAndPestsVo convertToVo(CropDiseasesAndPestsDocument doc) {
        CropDiseasesAndPestsVo vo = new CropDiseasesAndPestsVo();
        BeanUtils.copyProperties(doc, vo);
        return vo;
    }

    private DiseasesAndPestsVo convertToVo(CropDiseasesAndPests entity) {
        DiseasesAndPestsVo vo = new DiseasesAndPestsVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private CropDiseasesAndPestsDocument convertToDocument(CropDiseasesAndPests entity) {
        CropDiseasesAndPestsDocument doc = new CropDiseasesAndPestsDocument();
        BeanUtils.copyProperties(entity, doc);
        return doc;
    }

    /**
     * 计算两个字符串的相似度（基于公共字符前缀比例）
     */
    private double calculateSimilarity(String keyword, String name) {
        if (keyword == null || name == null) {
            return 0.0;
        }
        int keywordLength = keyword.length();
        int nameLength = name.length();
        int maxLen = Math.max(keywordLength, nameLength);
        int commonChars = 0;

        for (int i = 0; i < Math.min(keywordLength, nameLength); i++) {
            if (keyword.charAt(i) == name.charAt(i)) {
                commonChars++;
            }
        }
        return (double) commonChars / maxLen;
    }
    private DiseasesAndPestsVo convertDocumentToVo(CropDiseasesAndPestsDocument doc) {
        DiseasesAndPestsVo vo = new DiseasesAndPestsVo();
        BeanUtils.copyProperties(doc, vo);
        return vo;
    }

    private DiseasesAndPestsVo convertEntityToVo(CropDiseasesAndPests entity) {
        DiseasesAndPestsVo vo = new DiseasesAndPestsVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}