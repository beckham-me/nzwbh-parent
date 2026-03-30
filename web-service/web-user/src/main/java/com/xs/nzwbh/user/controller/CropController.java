package com.xs.nzwbh.user.controller;

import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.crop.client.CropFeignClient;
import com.xs.nzwbh.model.vo.CropVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作物信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/crop")
public class CropController {

    @Autowired
    private CropFeignClient cropFeignClient;

    /**
     * 根据ID获取作物详情
     * @param id 作物ID
     * @return 统一返回结果，包含作物信息
     */
    @GetMapping("/{id}")
    public Result<CropVo> getCropById(@PathVariable Long id) {
        log.info("获取作物详情，id: {}", id);
        try {
            Result<CropVo> result = cropFeignClient.getCropById(id);
            if (result == null) {
                log.warn("调用作物服务返回结果为空，id: {}", id);
                return Result.failMessage("获取作物信息失败，请稍后重试");
            }
            Integer code = result.getCode();
            if (code == null || code != 200) {
                log.warn("获取作物信息失败，id: {}, code: {}, message: {}", id, code, result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取作物信息失败";
                return Result.failMessage(errorMsg);
            }
            return result;
        } catch (Exception e) {
            log.error("调用作物服务异常，id: {}", id, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 获取所有作物列表
     * @return 统一返回结果，包含作物列表
     */
    @GetMapping
    public Result<List<CropVo>> getAllCrops() {
        log.info("获取所有作物列表");
        try {
            Result<List<CropVo>> result = cropFeignClient.getAllPlants();
            if (result == null) {
                log.warn("调用作物服务返回结果为空");
                return Result.failMessage("获取作物列表失败，请稍后重试");
            }
            Integer code = result.getCode();
            if (code == null || code != 200) {
                log.warn("获取作物列表失败，code: {}, message: {}", code, result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取作物列表失败";
                return Result.failMessage(errorMsg);
            }
            return result;
        } catch (Exception e) {
            log.error("调用作物服务异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}
