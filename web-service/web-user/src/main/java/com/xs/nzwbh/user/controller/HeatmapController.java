package com.xs.nzwbh.user.controller;

import com.xs.nzwbh.map.client.MapFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 热力图控制器
 */
@Slf4j
@RestController
public class HeatmapController {

    @Autowired
    private MapFeignClient mapFeignClient;

    /**
     * 获取默认地图中心点坐标（带缓存）
     * @return 中心点坐标 {latitude, longitude}
     */
    @Cacheable(value = "latestDetection", key = "'latest'")
    @GetMapping("/alert_status")
    public Map<String, Double> getCenter() {
        log.info("获取地图中心点坐标");
        try {
            Map<String, Double> center = mapFeignClient.getCenter();
            if (center != null && center.containsKey("latitude") && center.containsKey("longitude")) {
                log.debug("获取中心点成功: {}", center);
                return center;
            } else {
                log.warn("远程服务返回无效中心点，使用默认坐标");
                return getDefaultCenter();
            }
        } catch (Exception e) {
            log.error("获取中心点失败，调用远程服务异常", e);
            return getDefaultCenter();
        }
    }

    /**
     * 获取热力图数据
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 热力图数据（Map结构）
     */
    @GetMapping("/heatmap_data")
    public Map<String, Object> getHeatmapData(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        log.info("获取热力图数据，startTime: {}, endTime: {}", startTime, endTime);

        // 参数预处理：如果时间为空，可设置默认值（根据业务需要）
        // 示例：默认查询最近7天
        if (startTime == null && endTime == null) {
            log.debug("时间参数为空，将查询最近7天数据");
            // 可选：设置默认时间范围，但此处保持原逻辑，由feign接口处理
        }

        try {
            Map<String, Object> data = mapFeignClient.getHeatmapData(startTime, endTime);
            if (data == null) {
                log.warn("远程服务返回热力图数据为空，返回空Map");
                return new HashMap<>();
            }
            return data;
        } catch (Exception e) {
            log.error("获取热力图数据失败，调用远程服务异常", e);
            // 返回空Map，避免前端解析错误
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "数据获取失败");
            return errorMap;
        }
    }

    /**
     * 获取默认中心点坐标
     * @return 默认坐标（上海）
     */
    private Map<String, Double> getDefaultCenter() {
        Map<String, Double> defaultCenter = new HashMap<>();
        defaultCenter.put("latitude", 31.2304);
        defaultCenter.put("longitude", 121.4737);
        return defaultCenter;
    }
}
