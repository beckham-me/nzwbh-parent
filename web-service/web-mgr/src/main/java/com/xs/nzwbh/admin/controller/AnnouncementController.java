package com.xs.nzwbh.admin.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.announcement.client.AnnouncementFeignClient;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.AnnouncementDto;
import com.xs.nzwbh.model.vo.AnnouncementVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementFeignClient announcementFeignClient;

    /**
     * 分页获取公告列表
     *
     * @param page    当前页码（默认1，最小1）
     * @param size    每页条数（默认10，最小1）
     * @param keyword 搜索关键词
     * @return 公告分页数据
     */
    @GetMapping
    public Result<Page<AnnouncementVo>> getAnnouncements(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String keyword) {
        log.info("获取公告列表，参数: page={}, size={}, keyword={}", page, size, keyword);

        try {
            // 调用远程服务
            Result<Page<AnnouncementVo>> result = announcementFeignClient.getAnnouncements(page, size, keyword);
            if (result == null) {
                log.error("获取公告列表远程服务返回结果为空");
                return Result.failMessage("获取公告列表失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("获取公告列表失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取公告列表失败";
                return Result.failMessage(errorMsg);
            }
            Page<AnnouncementVo> data = result.getData();
            if (data == null) {
                log.warn("获取公告列表返回数据为空");
                return Result.ok(new Page<>()); // 返回空分页对象
            }
            return Result.ok(data);
        } catch (Exception e) {
            log.error("获取公告列表异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 新增公告
     *
     * @param announcementDto 公告数据传输对象
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<String> addAnnouncement(@Valid @RequestBody AnnouncementDto announcementDto) {
        log.info("添加公告，参数: {}", announcementDto);
        try {
            // 调用远程服务
            Result<Void> result = announcementFeignClient.addAnnouncement(announcementDto);
            if (result == null) {
                log.error("添加公告远程服务返回结果为空");
                return Result.failMessage("添加公告失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("添加公告失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "添加公告失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("公告添加成功");
        } catch (Exception e) {
            log.error("添加公告异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 更新公告
     *
     * @param announcementDto 公告数据传输对象（必须包含id）
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> updateAnnouncement(@Valid @RequestBody AnnouncementDto announcementDto) {
        log.info("更新公告，参数: {}", announcementDto);
        // 校验ID是否存在
        if (announcementDto.getId() == null || announcementDto.getId() <= 0) {
            log.warn("更新公告时ID无效: {}", announcementDto.getId());
            return Result.failMessage("公告ID不能为空");
        }
        try {
            Result<Void> result = announcementFeignClient.updateAnnouncement(announcementDto);
            if (result == null) {
                log.error("更新公告远程服务返回结果为空");
                return Result.failMessage("更新公告失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("更新公告失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "更新公告失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("公告更新成功");
        } catch (Exception e) {
            log.error("更新公告异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteAnnouncement(@PathVariable Long id) {
        log.info("删除公告，参数: id={}", id);
        if (id == null || id <= 0) {
            log.warn("删除公告时ID无效: {}", id);
            return Result.failMessage("公告ID无效");
        }
        try {
            Result<Void> result = announcementFeignClient.deleteAnnouncement(id);
            if (result == null) {
                log.error("删除公告远程服务返回结果为空");
                return Result.failMessage("删除公告失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("删除公告失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "删除公告失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("公告删除成功");
        } catch (Exception e) {
            log.error("删除公告异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}

