package com.xs.nzwbh.detect.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.detect.service.PestDetectionService;
import com.xs.nzwbh.model.entity.PestDetection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class HeatmapController {

    @Autowired
    private PestDetectionService pestDetectionService;

    /**
     * 获取最新虫害预警位置（用于地图中心点）
     */
    @GetMapping("/alert_status")
    public Map<String, Double> getCenter() {
        PestDetection latest = pestDetectionService.getLatestDetection().orElse(null);
        if (latest != null) {
            return Map.of(
                    "latitude", latest.getLatitude(),
                    "longitude", latest.getLongitude()
            );
        } else {
            // 默认返回一个默认坐标（可根据实际情况调整）
            return Map.of("latitude", 31.2304, "longitude", 121.4737);
        }
    }

    /**
     * 获取热力图数据（前端无分页参数时默认返回前500条）
     * 若前端需要分页，可增加 page、size 参数
     */
    @GetMapping("/heatmap_data")
    public Map<String, Object> getHeatmapData(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        // 默认查询第一页，每页 500 条（与之前硬限制保持一致）
        int page = 1;
        int size = 500;
        Page<PestDetection> pageResult = pestDetectionService.getAllForHeatmap(startTime, endTime, page, size);

        List<Map<String, Object>> data = pageResult.getRecords().stream()
                .map(pd -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("latitude", pd.getLatitude());
                    point.put("longitude", pd.getLongitude());
                    point.put("intensity", pd.getLevel());
                    return point;
                })
                .collect(Collectors.toList());

        // 返回与前端期望一致的格式
        return Map.of("data", data);
    }
}