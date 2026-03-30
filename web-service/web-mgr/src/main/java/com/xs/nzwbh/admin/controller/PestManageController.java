package com.xs.nzwbh.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.CropDiseasesAndPestsDto;
import com.xs.nzwbh.model.mgrvo.DiseasesAndPestsVo;
import com.xs.nzwbh.pest.client.CropPestFeignClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 病虫害管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/pest")
public class PestManageController {

    @Autowired
    private CropPestFeignClient cropPestFeignClient;

    /**
     * 分页获取病虫害列表
     *
     * @param page    当前页码（默认1，最小1）
     * @param size    每页条数（默认10，最小1）
     * @param keyword 搜索关键词（可选）
     * @return 分页数据
     */
    @GetMapping
    public Result<Page<DiseasesAndPestsVo>> getCropDiseasesAndPests(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String keyword) {
        log.info("获取病虫害列表，参数: page={}, size={}, keyword={}", page, size, keyword);

        try {
            Result<Page<DiseasesAndPestsVo>> result = cropPestFeignClient.getCropDiseasesAndPests(page, size, keyword);
            if (result == null) {
                log.error("获取病虫害列表远程服务返回结果为空");
                return Result.failMessage("获取病虫害列表失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("获取病虫害列表失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取病虫害列表失败";
                return Result.failMessage(errorMsg);
            }
            Page<DiseasesAndPestsVo> data = result.getData();
            if (data == null) {
                log.warn("获取病虫害列表返回数据为空");
                return Result.ok(new Page<>()); // 返回空分页对象
            }
            return Result.ok(data);
        } catch (Exception e) {
            log.error("获取病虫害列表异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 新增病虫害信息
     *
     * @param dto 病虫害数据传输对象
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<String> addCrop(@Valid @RequestBody CropDiseasesAndPestsDto dto) {
        log.info("添加病虫害信息，参数: {}", dto);
        try {
            Result<Void> result = cropPestFeignClient.addCropDiseaseAndPest(dto);
            if (result == null) {
                log.error("添加病虫害远程服务返回结果为空");
                return Result.failMessage("添加病虫害失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("添加病虫害失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "添加病虫害失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("病虫害信息添加成功");
        } catch (Exception e) {
            log.error("添加病虫害异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 更新病虫害信息
     *
     * @param dto 病虫害数据传输对象（必须包含id）
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> updateCrop(@Valid @RequestBody CropDiseasesAndPestsDto dto) {
        log.info("更新病虫害信息，参数: {}", dto);
        // 校验ID是否存在
        if (dto.getId() == null || dto.getId() <= 0) {
            log.warn("更新病虫害时ID无效: {}", dto.getId());
            return Result.failMessage("病虫害ID不能为空");
        }
        try {
            Result<Void> result = cropPestFeignClient.updateCropDiseaseAndPest(dto);
            if (result == null) {
                log.error("更新病虫害远程服务返回结果为空");
                return Result.failMessage("更新病虫害失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("更新病虫害失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "更新病虫害失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("病虫害信息更新成功");
        } catch (Exception e) {
            log.error("更新病虫害异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 删除病虫害信息
     *
     * @param id 病虫害ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteCrop(@PathVariable Long id) {
        log.info("删除病虫害信息，参数: id={}", id);
        if (id == null || id <= 0) {
            log.warn("删除病虫害时ID无效: {}", id);
            return Result.failMessage("病虫害ID无效");
        }
        try {
            Result<Void> result = cropPestFeignClient.deleteCropDiseaseAndPest(id);
            if (result == null) {
                log.error("删除病虫害远程服务返回结果为空");
                return Result.failMessage("删除病虫害失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("删除病虫害失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "删除病虫害失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("病虫害信息删除成功");
        } catch (Exception e) {
            log.error("删除病虫害异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}
