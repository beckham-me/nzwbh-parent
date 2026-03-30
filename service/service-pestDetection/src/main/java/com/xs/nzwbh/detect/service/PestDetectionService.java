package com.xs.nzwbh.detect.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.model.entity.PestDetection;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PestDetectionService {

    /**
     * 获取最近一条虫害记录，用于定位地图中心
     */
    Optional<PestDetection> getLatestDetection();

    Page<PestDetection> getAllForHeatmap(LocalDateTime startTime,
                                         LocalDateTime endTime,
                                         int page,
                                         int size);
}
