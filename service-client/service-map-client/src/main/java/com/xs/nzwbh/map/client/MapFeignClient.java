package com.xs.nzwbh.map.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Map;

@FeignClient(value = "service-pestDetection")
public interface MapFeignClient {
    @GetMapping("/alert_status")
    public Map<String, Double> getCenter();
    @GetMapping("/heatmap_data")
    public Map<String, Object> getHeatmapData(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    );
}
