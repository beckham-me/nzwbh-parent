package com.xs.nzwbh.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.mgrvo.UserVo;
import com.xs.nzwbh.user.client.UserFeignClient;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 用户管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/user")
public class UserManagermentController {

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 分页获取用户列表
     *
     * @param page    当前页码（默认1，最小1）
     * @param size    每页条数（默认10，最小1）
     * @param keyword 搜索关键词（可选）
     * @return 用户分页数据
     */
    @GetMapping
    public Result<Page<UserVo>> getUsers(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String keyword) {
        log.info("获取用户列表，参数: page={}, size={}, keyword={}", page, size, keyword);

        try {
            Result<Page<UserVo>> result = userFeignClient.getUsers(page, size, keyword);
            if (result == null) {
                log.error("获取用户列表远程服务返回结果为空");
                return Result.failMessage("获取用户列表失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("获取用户列表失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取用户列表失败";
                return Result.failMessage(errorMsg);
            }
            Page<UserVo> data = result.getData();
            if (data == null) {
                log.warn("获取用户列表返回数据为空");
                return Result.ok(new Page<>()); // 返回空分页对象
            }
            return Result.ok(data);
        } catch (Exception e) {
            log.error("获取用户列表异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}