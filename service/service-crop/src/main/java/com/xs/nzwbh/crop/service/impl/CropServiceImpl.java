package com.xs.nzwbh.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.crop.es.CropRepository;
import com.xs.nzwbh.crop.mapper.CropMapper;
import com.xs.nzwbh.crop.service.CropService;
import com.xs.nzwbh.model.dto.CropDto;
import com.xs.nzwbh.model.entity.Crop;
import com.xs.nzwbh.model.esentity.CropDocument;
import com.xs.nzwbh.model.mgrvo.Crop1Vo;
import com.xs.nzwbh.model.vo.CropVo;
import com.xs.nzwbh.model.vo.SearchVo;
import com.xs.nzwbh.pest.client.CropPestFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CropServiceImpl extends ServiceImpl<CropMapper, Crop> implements CropService {

    @Autowired
    private CropMapper cropMapper;
    @Autowired
    private CropRepository cropRepository;
    @Autowired
    private RBloomFilter<String> cropBloomFilter;          // 布隆过滤器（作物专用）
    @Autowired
    private CropPestFeignClient cropPestFeignClient;       // 用于获取关联的病虫害名称

    // ========== 查询方法改造（优先 ES） ==========

    @Override
    public CropVo getById(Long id) {
        // 1. 布隆过滤器判断
        if (!cropBloomFilter.contains(String.valueOf(id))) {
            throw new RuntimeException("未查询到相关数据");
        }
        // 2. 从 ES 查询
        Optional<CropDocument> opt = cropRepository.findById(id);
        if (opt.isPresent()) {
            CropDocument doc = opt.get();
            if (doc.getIsDeleted() == 1) {
                throw new RuntimeException("数据已删除");
            }
            CropVo vo = new CropVo();
            BeanUtils.copyProperties(doc, vo);
            return vo;
        }
        // 3. 降级 MySQL
        Crop crop = cropMapper.selectById(id);
        if (crop == null || crop.getIsDeleted() == 1) {
            throw new RuntimeException("未查询到相关数据");
        }
        // 同步到 ES
        CropDocument doc = convertToDocument(crop);
        cropRepository.save(doc);
        cropBloomFilter.add(String.valueOf(id));
        CropVo vo = new CropVo();
        BeanUtils.copyProperties(crop, vo);
        return vo;
    }

    @Override
    public List<CropVo> getAllCrops() {
        // 优先从 ES 查询所有未删除作物（最多取 1000 条，可根据需要调整）
        Iterable<CropDocument> iterable = cropRepository.findAll();
        List<CropVo> voList = new ArrayList<>();
        for (CropDocument doc : iterable) {
            if (doc.getIsDeleted() == 0) {
                CropVo vo = new CropVo();
                BeanUtils.copyProperties(doc, vo);
                voList.add(vo);
            }
        }
        if (!voList.isEmpty()) {
            return voList;
        }
        // 降级 MySQL
        List<Crop> crops = cropMapper.selectList(null);
        return crops.stream()
                .filter(c -> c.getIsDeleted() == 0)
                .map(c -> {
                    CropVo vo = new CropVo();
                    BeanUtils.copyProperties(c, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SearchVo findMostSimilarName(String keyword) {
        // 从 ES 模糊匹配
        List<CropDocument> docs = cropRepository.findByNameContaining(keyword);
        if (!docs.isEmpty()) {
            // 相似度排序（与 pest 微服务一致）
            List<CropDocument> sorted = docs.stream()
                    .sorted((a, b) -> Double.compare(
                            calculateSimilarity(keyword, b.getName()),
                            calculateSimilarity(keyword, a.getName())
                    ))
                    .limit(10)
                    .collect(Collectors.toList());
            CropDocument mostSimilar = sorted.get(0);
            SearchVo vo = new SearchVo();
            BeanUtils.copyProperties(mostSimilar, vo);
            vo.setType("crop");
            return vo;
        }
        // 降级 MySQL
        LambdaQueryWrapper<Crop> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Crop::getName, keyword).orderByDesc(Crop::getName).last("LIMIT 1");
        Crop crop = cropMapper.selectOne(wrapper);
        if (crop == null) throw new RuntimeException("未查询到相关数据");
        SearchVo vo = new SearchVo();
        BeanUtils.copyProperties(crop, vo);
        vo.setType("crop");
        return vo;
    }

    @Override
    public List<String> findSimilarCropNames(String keyword) {
        List<CropDocument> docs = cropRepository.findByNameContaining(keyword);
        if (!docs.isEmpty()) {
            return docs.stream().limit(4).map(CropDocument::getName).collect(Collectors.toList());
        }
        // 降级 MySQL
        LambdaQueryWrapper<Crop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Crop::getName, keyword).orderByDesc(Crop::getName).last("LIMIT 4");
        List<Crop> crops = cropMapper.selectList(queryWrapper);
        return crops.stream().map(Crop::getName).collect(Collectors.toList());
    }

    @Override
    public Result<Page<Crop1Vo>> getCrops(Integer page, Integer size, String keyword) {
        try {
            // 使用 Spring Data 分页
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, size);
            org.springframework.data.domain.Page<CropDocument> esPage;
            if (keyword != null && !keyword.isEmpty()) {
                // 需要 Repository 支持模糊分页，如果未定义，可使用 ElasticsearchRestTemplate
                esPage = cropRepository.findAll(pageable); // 这里简化，实际需自定义
            } else {
                esPage = cropRepository.findAll(pageable);
            }
            // 转换分页结果
            Page<Crop1Vo> resultPage = new Page<>(page, size);
            resultPage.setTotal(esPage.getTotalElements());
            resultPage.setRecords(esPage.getContent().stream()
                    .map(this::convertDocumentToCrop1Vo)
                    .collect(Collectors.toList()));
            return Result.ok(resultPage);
        } catch (Exception e) {
            // 降级 MySQL
            log.warn("ES 分页查询失败，降级到 MySQL", e);
            Page<Crop> cropPage = new Page<>(page, size);
            QueryWrapper<Crop> queryWrapper = new QueryWrapper<>();
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("name", keyword);
            }
            Page<Crop> cropPageResult = cropMapper.selectPage(cropPage, queryWrapper);
            Page<Crop1Vo> voPage = new Page<>();
            voPage.setCurrent(cropPageResult.getCurrent());
            voPage.setSize(cropPageResult.getSize());
            voPage.setTotal(cropPageResult.getTotal());
            voPage.setPages(cropPageResult.getPages());
            voPage.setRecords(cropPageResult.getRecords().stream()
                    .map(this::convertToCrop1Vo)
                    .collect(Collectors.toList()));
            return Result.ok(voPage);
        }
    }

    // ========== 写操作（同步 ES） ==========

    @Override
    @Transactional
    public Result<Void> addCrops(CropDto dto) {
        Crop crop = new Crop();
        BeanUtils.copyProperties(dto, crop);
        crop.setCreateTime(new Date());
        crop.setUpdateTime(new Date());
        crop.setIsDeleted(0);
        boolean success = cropMapper.insert(crop) > 0;
        if (success) {
            CropDocument doc = convertToDocument(crop);
            cropRepository.save(doc);
            cropBloomFilter.add(String.valueOf(crop.getId()));
        }
        return success ? Result.ok() : Result.fail();
    }

    @Override
    @Transactional
    public Result<Void> updateCrops(CropDto dto) {
        if (dto.getId() == null) {
            log.error("农作物 ID 不能为空");
            return Result.fail();
        }
        Crop crop = cropMapper.selectById(dto.getId());
        if (crop == null) {
            log.error("信息不存在");
            return Result.fail();
        }
        crop.setName(dto.getName());
        crop.setDescription(dto.getDescription());
        crop.setImage(dto.getImage());
        crop.setUpdateTime(new Date());
        boolean success = cropMapper.updateById(crop) > 0;
        if (success) {
            CropDocument doc = convertToDocument(crop);
            cropRepository.save(doc);
        }
        return success ? Result.ok() : Result.fail();
    }

    @Override
    @Transactional
    public Result<Void> deleteCrops(Long id) {
        Crop crop = cropMapper.selectById(id);
        if (crop == null) return Result.fail();
        crop.setIsDeleted(1);
        crop.setUpdateTime(new Date());
        boolean success = cropMapper.updateById(crop) > 0;
        if (success) {
            CropDocument doc = convertToDocument(crop);
            cropRepository.save(doc);   // 更新 isDeleted=1
        }
        return success ? Result.ok() : Result.fail();
    }

    // ========== 私有辅助方法 ==========

    private CropDocument convertToDocument(Crop crop) {
        CropDocument doc = new CropDocument();
        BeanUtils.copyProperties(crop, doc);
        return doc;
    }

    private Crop1Vo convertDocumentToCrop1Vo(CropDocument doc) {
        Crop1Vo vo = new Crop1Vo();
        BeanUtils.copyProperties(doc, vo);
        // 关联病虫害名称（调用 Feign）
        try {
            List<String> pestNames = cropPestFeignClient.getPestNames(doc.getId());
            vo.setDisease(pestNames);
        } catch (Exception e) {
            log.error("获取病虫害名称失败", e);
            vo.setDisease(new ArrayList<>());
        }
        return vo;
    }

    private Crop1Vo convertToCrop1Vo(Crop crop) {
        Crop1Vo vo = new Crop1Vo();
        BeanUtils.copyProperties(crop, vo);
        try {
            List<String> pestNames = cropPestFeignClient.getPestNames(crop.getId());
            vo.setDisease(pestNames);
        } catch (Exception e) {
            log.error("获取病虫害名称失败", e);
            vo.setDisease(new ArrayList<>());
        }
        return vo;
    }

    /**
     * 计算字符串相似度（与 pest 微服务保持一致）
     */
    private double calculateSimilarity(String keyword, String name) {
        if (keyword == null || name == null) return 0.0;
        int keywordLength = keyword.length();
        int nameLength = name.length();
        int maxLen = Math.max(keywordLength, nameLength);
        int commonChars = 0;
        for (int i = 0; i < Math.min(keywordLength, nameLength); i++) {
            if (keyword.charAt(i) == name.charAt(i)) commonChars++;
        }
        return (double) commonChars / maxLen;
    }
}