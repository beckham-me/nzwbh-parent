package com.xs.nzwbh.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.crop.client.CropFeignClient;
import com.xs.nzwbh.model.dto.CropDto;
import com.xs.nzwbh.model.mgrvo.Crop1Vo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 农作物管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/crop")
public class CropManageController {

    @Autowired
    private CropFeignClient cropFeignClient;

    /**
     * 分页获取农作物列表
     *
     * @param page    当前页码（默认1，最小1）
     * @param size    每页条数（默认10，最小1）
     * @param keyword 搜索关键词
     * @return 农作物分页数据
     */
    @GetMapping
    public Result<Page<Crop1Vo>> getCrops(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String keyword) {
        log.info("获取农作物列表，参数: page={}, size={}, keyword={}", page, size, keyword);

        try {
            Result<Page<Crop1Vo>> result = cropFeignClient.getCrops(page, size, keyword);
            if (result == null) {
                log.error("获取农作物列表远程服务返回结果为空");
                return Result.failMessage("获取农作物列表失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("获取农作物列表失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取农作物列表失败";
                return Result.failMessage(errorMsg);
            }
            Page<Crop1Vo> data = result.getData();
            if (data == null) {
                log.warn("获取农作物列表返回数据为空");
                return Result.ok(new Page<>()); // 返回空分页对象
            }
            return Result.ok(data);
        } catch (Exception e) {
            log.error("获取农作物列表异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 新增农作物
     *
     * @param cropDto 农作物数据传输对象
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<String> addCrop(@Valid @RequestBody CropDto cropDto) {
        log.info("添加农作物信息，参数: {}", cropDto);
        try {
            Result<Void> result = cropFeignClient.addCrop(cropDto);
            if (result == null) {
                log.error("添加农作物远程服务返回结果为空");
                return Result.failMessage("添加农作物失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("添加农作物失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "添加农作物失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("农作物信息添加成功");
        } catch (Exception e) {
            log.error("添加农作物异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 更新农作物信息
     *
     * @param cropDto 农作物数据传输对象（必须包含id）
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> updateCrop(@Valid @RequestBody CropDto cropDto) {
        log.info("更新农作物信息，参数: {}", cropDto);
        // 校验ID是否存在
        if (cropDto.getId() == null || cropDto.getId() <= 0) {
            log.warn("更新农作物时ID无效: {}", cropDto.getId());
            return Result.failMessage("农作物ID不能为空");
        }
        try {
            Result<Void> result = cropFeignClient.updateCrop(cropDto);
            if (result == null) {
                log.error("更新农作物远程服务返回结果为空");
                return Result.failMessage("更新农作物失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("更新农作物失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "更新农作物失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("农作物信息更新成功");
        } catch (Exception e) {
            log.error("更新农作物异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 删除农作物信息
     *
     * @param id 农作物ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteCrop(@PathVariable Long id) {
        log.info("删除农作物信息，参数: id={}", id);
        if (id == null || id <= 0) {
            log.warn("删除农作物时ID无效: {}", id);
            return Result.failMessage("农作物ID无效");
        }
        try {
            Result<Void> result = cropFeignClient.deleteCrop(id);
            if (result == null) {
                log.error("删除农作物远程服务返回结果为空");
                return Result.failMessage("删除农作物失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("删除农作物失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "删除农作物失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("农作物信息删除成功");
        } catch (Exception e) {
            log.error("删除农作物异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}
