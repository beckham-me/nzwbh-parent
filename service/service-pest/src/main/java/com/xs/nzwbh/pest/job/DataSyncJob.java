package com.xs.nzwbh.pest.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.model.entity.CropDiseasesAndPests;
import com.xs.nzwbh.model.esentity.CropDiseasesAndPestsDocument;
import com.xs.nzwbh.pest.es.repository.CropDiseasesAndPestsRepository;
import com.xs.nzwbh.pest.mapper.CropPestMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataSyncJob {

    @Autowired
    private CropPestMapper cropPestMapper;
    @Autowired
    private CropDiseasesAndPestsRepository esRepository;

    @XxlJob("syncAllPestToES")
    public void syncAllDataToES() {
        XxlJobHelper.log("开始全量同步病虫害数据到 ES");
        int pageSize = 1000;
        int currentPage = 1;
        long total = 0;
        do {
            Page<CropDiseasesAndPests> page = new Page<>(currentPage, pageSize);
            Page<CropDiseasesAndPests> result = cropPestMapper.selectPage(page,
                    new LambdaQueryWrapper<CropDiseasesAndPests>().eq(CropDiseasesAndPests::getIsDeleted, 0));
            List<CropDiseasesAndPests> list = result.getRecords();
            if (!list.isEmpty()) {
                List<CropDiseasesAndPestsDocument> documents = list.stream()
                        .map(this::convertToDocument)
                        .collect(Collectors.toList());
                esRepository.saveAll(documents);
                XxlJobHelper.log("同步第 {} 页，共 {} 条", currentPage, list.size());
            }
            total = result.getTotal();
            currentPage++;
        } while ((currentPage - 1) * pageSize < total);
        XxlJobHelper.log("全量同步完成，共 {} 条数据", total);
    }

    @XxlJob("checkAndFixPestES")
    public void checkAndFixES() {
        XxlJobHelper.log("开始校验 ES 与 MySQL 一致性");
        // 获取 MySQL 有效ID
        List<Long> mysqlIds = cropPestMapper.selectList(
                        new LambdaQueryWrapper<CropDiseasesAndPests>()
                                .eq(CropDiseasesAndPests::getIsDeleted, 0)
                                .select(CropDiseasesAndPests::getId))
                .stream()
                .map(CropDiseasesAndPests::getId)
                .collect(Collectors.toList());

        // 获取 ES 中所有ID（数据量大需分页，这里简化）
        Iterable<CropDiseasesAndPestsDocument> all = esRepository.findAll();
        Set<Long> esIds = new HashSet<>();
        all.forEach(doc -> esIds.add(doc.getId()));

        // 补录缺失
        List<Long> missingInES = mysqlIds.stream().filter(id -> !esIds.contains(id)).collect(Collectors.toList());
        if (!missingInES.isEmpty()) {
            List<CropDiseasesAndPests> missingData = cropPestMapper.selectBatchIds(missingInES);
            List<CropDiseasesAndPestsDocument> docs = missingData.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());
            esRepository.saveAll(docs);
            XxlJobHelper.log("补录 {} 条", docs.size());
        }

        // 清理多余
        List<Long> missingInMySQL = esIds.stream().filter(id -> !mysqlIds.contains(id)).collect(Collectors.toList());
        if (!missingInMySQL.isEmpty()) {
            esRepository.deleteAllById(missingInMySQL);
            XxlJobHelper.log("清理 {} 条", missingInMySQL.size());
        }

        XxlJobHelper.log("一致性校验完成");
    }

    private CropDiseasesAndPestsDocument convertToDocument(CropDiseasesAndPests entity) {
        CropDiseasesAndPestsDocument doc = new CropDiseasesAndPestsDocument();
        BeanUtils.copyProperties(entity, doc);
        return doc;
    }
}
