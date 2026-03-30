package com.xs.nzwbh.crop.job;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.crop.es.CropRepository;
import com.xs.nzwbh.crop.mapper.CropMapper;
import com.xs.nzwbh.model.entity.Crop;
import com.xs.nzwbh.model.esentity.CropDocument;
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
    private CropMapper cropMapper;
    @Autowired
    private CropRepository cropRepository;

    @XxlJob("syncAllCropToES")
    public void syncAllDataToES() {
        XxlJobHelper.log("开始全量同步作物数据到 ES");
        int pageSize = 1000;
        int currentPage = 1;
        long total = 0;
        do {
            Page<Crop> page = new Page<>(currentPage, pageSize);
            Page<Crop> result = cropMapper.selectPage(page,
                    new LambdaQueryWrapper<Crop>().eq(Crop::getIsDeleted, 0));
            List<Crop> list = result.getRecords();
            if (!list.isEmpty()) {
                List<CropDocument> documents = list.stream()
                        .map(this::convertToDocument)
                        .collect(Collectors.toList());
                cropRepository.saveAll(documents);
                XxlJobHelper.log("同步第 {} 页，共 {} 条", currentPage, list.size());
            }
            total = result.getTotal();
            currentPage++;
        } while ((currentPage - 1) * pageSize < total);
        XxlJobHelper.log("全量同步完成，共 {} 条数据", total);
    }

    @XxlJob("checkAndFixCropES")
    public void checkAndFixES() {
        XxlJobHelper.log("开始校验作物 ES 与 MySQL 一致性");

        List<Long> mysqlIds = cropMapper.selectList(
                        new LambdaQueryWrapper<Crop>().eq(Crop::getIsDeleted, 0).select(Crop::getId))
                .stream()
                .map(Crop::getId)
                .collect(Collectors.toList());

        Iterable<CropDocument> all = cropRepository.findAll();
        Set<Long> esIds = new HashSet<>();
        all.forEach(doc -> esIds.add(doc.getId()));

        // 补录缺失
        List<Long> missingInES = mysqlIds.stream().filter(id -> !esIds.contains(id)).collect(Collectors.toList());
        if (!missingInES.isEmpty()) {
            List<Crop> missingData = cropMapper.selectBatchIds(missingInES);
            List<CropDocument> docs = missingData.stream().map(this::convertToDocument).collect(Collectors.toList());
            cropRepository.saveAll(docs);
            XxlJobHelper.log("补录 {} 条", docs.size());
        }

        // 清理多余
        List<Long> missingInMySQL = esIds.stream().filter(id -> !mysqlIds.contains(id)).collect(Collectors.toList());
        if (!missingInMySQL.isEmpty()) {
            cropRepository.deleteAllById(missingInMySQL);
            XxlJobHelper.log("清理 {} 条", missingInMySQL.size());
        }

        XxlJobHelper.log("一致性校验完成");
    }

    private CropDocument convertToDocument(Crop crop) {
        CropDocument doc = new CropDocument();
        BeanUtils.copyProperties(crop, doc);
        return doc;
    }
}