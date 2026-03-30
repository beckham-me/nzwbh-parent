package com.xs.nzwbh.user.controller;

import com.xs.nzwbh.common.login.Login;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.vo.UserLoginVo;
import com.xs.nzwbh.user.client.UserFeignClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Tag(name = "客户API接口管理")
@RestController
@RequestMapping("/user")
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取当前登录用户信息
     *
     * @param token 用户令牌
     * @return 用户信息
     */
    @Operation(summary = "获取客户登录信息")
    @Login
    @GetMapping("/getUserLoginInfo")
    public Result<UserLoginVo> getUserLoginInfo(@RequestHeader(value = "token") String token) {
        log.info("获取用户登录信息，token: {}", token);

        try {
            // 1. token 校验
            if (token == null || token.trim().isEmpty()) {
                log.warn("token为空");
                return Result.failMessage("请先登录");
            }

            // 2. 从 Redis 获取用户 ID
            Object userIdObj = redisTemplate.opsForValue().get(token);
            if (userIdObj == null) {
                log.warn("token无效或已过期，token: {}", token);
                return Result.failMessage("登录已过期，请重新登录");
            }
            Long userId;
            try {
                userId = Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException e) {
                log.error("用户ID格式错误，token: {}, value: {}", token, userIdObj, e);
                return Result.failMessage("用户信息异常，请重新登录");
            }

            // 3. 调用远程服务获取用户信息
            Result<UserLoginVo> result = userFeignClient.getUserLoginInfo(userId);
            if (result == null) {
                log.error("远程服务返回结果为空，userId: {}", userId);
                return Result.failMessage("获取用户信息失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("获取用户信息失败，userId: {}, code: {}, message: {}", userId, result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取用户信息失败";
                return Result.failMessage(errorMsg);
            }
            UserLoginVo userLoginVo = result.getData();
            if (userLoginVo == null) {
                log.warn("用户信息不存在，userId: {}", userId);
                return Result.failMessage("用户信息不存在");
            }

            return Result.ok(userLoginVo);

        } catch (Exception e) {
            log.error("获取用户登录信息异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 微信小程序授权登录
     *
     * @param code 微信授权码
     * @return 用户令牌
     */
    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<String> wxLogin(@PathVariable String code) {
        log.info("微信登录，code: {}", code);

        try {
            // 参数校验
            if (code == null || code.trim().isEmpty()) {
                log.warn("微信登录code为空");
                return Result.failMessage("登录参数错误");
            }

            // 1. 调用远程登录服务
            Result<Long> loginResult = userFeignClient.login(code);
            if (loginResult == null) {
                log.error("远程登录服务返回结果为空，code: {}", code);
                return Result.failMessage("登录失败，请稍后重试");
            }
            if (loginResult.getCode() == null || loginResult.getCode().intValue() != 200) {
                log.warn("登录失败，code: {}, message: {}", loginResult.getCode(), loginResult.getMessage());
                String errorMsg = loginResult.getMessage() != null ? loginResult.getMessage() : "登录失败";
                return Result.failMessage(errorMsg);
            }
            Long userId = loginResult.getData();
            if (userId == null) {
                log.error("登录返回用户ID为空，code: {}", code);
                return Result.failMessage("登录失败，用户信息异常");
            }

            // 2. 生成 token 并存入 Redis
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            try {
                redisTemplate.opsForValue().set(token, userId.toString(), 30, TimeUnit.MINUTES);
                log.info("用户登录成功，userId: {}, token: {}", userId, token);
            } catch (Exception e) {
                log.error("Redis存储token失败，userId: {}", userId, e);
                return Result.failMessage("登录失败，请稍后重试");
            }

            return Result.ok(token);

        } catch (Exception e) {
            log.error("微信登录异常，code: {}", code, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}