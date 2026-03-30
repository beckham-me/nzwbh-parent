package com.xs.nzwbh.admin.controller;


import com.xs.nzwbh.admin.client.AdminFeignClient;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员登录/注册控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin")
public class LoginController {

    @Autowired
    private AdminFeignClient adminFeignClient;

    /**
     * 管理员登录
     *
     * @param request 登录请求参数（用户名、密码）
     * @return 操作结果
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("管理员登录请求，username: {}", request.getUsername());

        try {
            // 调用远程服务，直接返回 boolean 类型
            boolean success = adminFeignClient.login(request);
            if (success) {
                log.info("登录成功，username: {}", request.getUsername());
                // 可在此处生成 token 并返回
                return Result.ok("登录成功");
            } else {
                log.warn("登录失败，用户名或密码错误，username: {}", request.getUsername());
                return Result.failMessage("用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("登录异常，username: {}", request.getUsername(), e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 管理员注册
     *
     * @param request 注册请求参数（用户名、密码）
     * @return 操作结果
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody LoginRequest request) {
        log.info("管理员注册请求，username: {}", request.getUsername());

        try {
            // 调用远程服务，直接返回 boolean 类型
            boolean success = adminFeignClient.register(request);
            if (success) {
                log.info("注册成功，username: {}", request.getUsername());
                return Result.ok("注册成功");
            } else {
                log.warn("注册失败，用户名可能已存在，username: {}", request.getUsername());
                return Result.failMessage("用户名已存在");
            }
        } catch (Exception e) {
            log.error("注册异常，username: {}", request.getUsername(), e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}
