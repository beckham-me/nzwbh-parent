package com.xs.nzwbh.user.controller;


import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.vo.CropDiseasesAndPestsVo;
import com.xs.nzwbh.pest.client.CropPestFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作物病虫害信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/cropPest")
public class CropPestController {

    @Autowired
    private CropPestFeignClient cropPestFeignClient;

    /**
     * 根据作物ID获取病害信息列表
     * @param cropId 作物ID
     * @return 统一返回结果，包含病害列表
     */
    @GetMapping("/diseases/{cropId}")
    public Result<List<CropDiseasesAndPestsVo>> getDiseasesInfo(@PathVariable Long cropId) {
        log.info("获取病害信息，cropId: {}", cropId);
        try {
            Result<List<CropDiseasesAndPestsVo>> result = cropPestFeignClient.getDiseasesInfo(cropId);
            if (result == null) {
                log.warn("调用病虫害服务返回结果为空，cropId: {}", cropId);
                return Result.failMessage("获取病害信息失败，请稍后重试");
            }
            Integer code = result.getCode();
            if (code == null || code != 200) {
                log.warn("获取病害信息失败，cropId: {}, code: {}, message: {}", cropId, code, result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取病害信息失败";
                return Result.failMessage(errorMsg);
            }
            return result;
        } catch (Exception e) {
            log.error("调用病虫害服务异常，cropId: {}", cropId, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 根据作物ID获取虫害信息列表
     * @param cropId 作物ID
     * @return 统一返回结果，包含虫害列表
     */
    @GetMapping("/pests/{cropId}")
    public Result<List<CropDiseasesAndPestsVo>> getPestsInfo(@PathVariable Long cropId) {
        log.info("获取虫害信息，cropId: {}", cropId);
        try {
            Result<List<CropDiseasesAndPestsVo>> result = cropPestFeignClient.getPestsInfo(cropId);
            if (result == null) {
                log.warn("调用病虫害服务返回结果为空，cropId: {}", cropId);
                return Result.failMessage("获取虫害信息失败，请稍后重试");
            }
            Integer code = result.getCode();
            if (code == null || code != 200) {
                log.warn("获取虫害信息失败，cropId: {}, code: {}, message: {}", cropId, code, result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取虫害信息失败";
                return Result.failMessage(errorMsg);
            }
            return result;
        } catch (Exception e) {
            log.error("调用病虫害服务异常，cropId: {}", cropId, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 根据病虫害ID获取详细信息
     * @param id 病虫害ID
     * @return 统一返回结果，包含病虫害详情
     */
    @GetMapping("/diseasesandpests/{id}")
    public Result<CropDiseasesAndPestsVo> getDiseasesAndPestsInfo(@PathVariable Long id) {
        log.info("获取病虫害详情，id: {}", id);
        try {
            Result<CropDiseasesAndPestsVo> result = cropPestFeignClient.getCropDiseasesAndPestsInfo(id);
            if (result == null) {
                log.warn("调用病虫害服务返回结果为空，id: {}", id);
                return Result.failMessage("获取病虫害信息失败，请稍后重试");
            }
            Integer code = result.getCode();
            if (code == null || code != 200) {
                log.warn("获取病虫害详情失败，id: {}, code: {}, message: {}", id, code, result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取病虫害信息失败";
                return Result.failMessage(errorMsg);
            }
            return result;
        } catch (Exception e) {
            log.error("调用病虫害服务异常，id: {}", id, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 获取常见病虫害列表
     * @return 统一返回结果，包含常见病虫害列表
     */
    @GetMapping("/common")
    public Result<List<CropDiseasesAndPestsVo>> getCommonCropDiseasesAndPestsInfo() {
        log.info("获取常见病虫害列表");
        try {
            Result<List<CropDiseasesAndPestsVo>> result = cropPestFeignClient.getCommonCropDiseasesAndPestsInfo();
            if (result == null) {
                log.warn("调用病虫害服务返回结果为空");
                return Result.failMessage("获取常见病虫害列表失败，请稍后重试");
            }
            Integer code = result.getCode();
            if (code == null || code != 200) {
                log.warn("获取常见病虫害列表失败，code: {}, message: {}", code, result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取常见病虫害列表失败";
                return Result.failMessage(errorMsg);
            }
            return result;
        } catch (Exception e) {
            log.error("调用病虫害服务异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}
