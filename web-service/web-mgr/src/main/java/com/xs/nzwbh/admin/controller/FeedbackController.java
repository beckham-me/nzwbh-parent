package com.xs.nzwbh.admin.controller;


import com.xs.nzwbh.common.result.PageResult;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.vo.FeedbackVO;
import com.xs.nzwbh.user.client.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户反馈管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 分页获取反馈列表
     *
     * @param page  当前页码（默认1，最小1）
     * @param limit 每页条数（默认10，最小1，最大100）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<List<FeedbackVO>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("获取反馈列表，page={}, limit={}", page, limit);

        // 参数校验
        int validPage = Math.max(page, 1);
        int validLimit = Math.min(Math.max(limit, 1), 100); // 限制最大100条

        if (validPage != page || validLimit != limit) {
            log.info("分页参数自动修正，原page={},limit={} → 修正为page={},limit={}", page, limit, validPage, validLimit);
        }

        try {
            Result<PageResult<List<FeedbackVO>>> result = userFeignClient.page(validPage, validLimit);
            if (result == null) {
                log.error("远程服务返回结果为空");
                return Result.failMessage("获取反馈列表失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("获取反馈列表失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取反馈列表失败";
                return Result.failMessage(errorMsg);
            }
            return result;
        } catch (Exception e) {
            log.error("获取反馈列表异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 删除反馈
     *
     * @param id 反馈ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        log.info("删除反馈，id={}", id);
        if (id == null || id <= 0) {
            log.warn("删除反馈时ID无效: {}", id);
            return Result.failMessage("反馈ID无效");
        }
        try {
            Result<?> result = userFeignClient.delete(id);
            if (result == null) {
                log.error("远程服务返回结果为空，id={}", id);
                return Result.failMessage("删除反馈失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("删除反馈失败，id={}, code: {}, message: {}", id, result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "删除反馈失败";
                return Result.failMessage(errorMsg);
            }
            return result;
        } catch (Exception e) {
            log.error("删除反馈异常，id={}", id, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}
