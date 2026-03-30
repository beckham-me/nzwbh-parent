package com.xs.nzwbh.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.entity.User;
import com.xs.nzwbh.model.mgrvo.UserVo;
import com.xs.nzwbh.model.vo.UserLoginVo;
import com.xs.nzwbh.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取客户登录信息")
    @GetMapping("/getUserLoginInfo/{userId}")
    public Result<UserLoginVo> getUserLoginInfo(@PathVariable Long userId) {
        UserLoginVo userLoginVo = userService.getUserLoginInfo(userId);
        return Result.ok(userLoginVo);
    }

    //微信小程序登录接口
    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<Long> login(@PathVariable String code) {
        return Result.ok(userService.login(code));
    }

    @Operation(summary = "获取客户OpenId")
    @GetMapping("/getUserOpenId/{userId}")
    public Result<String> getUserOpenId(@PathVariable Long userId) {
        return Result.ok(userService.getUserOpenId(userId));
    }

    @GetMapping("/getUsername")
    public List<User> getUsersByIds(@RequestParam("userIds") List<Long> userIds){
        return userService.getUsersByIds(userIds);
    }

    //管理员端
    @GetMapping
    public Result<Page<UserVo>> getUsers(@RequestParam Integer page,
                                                 @RequestParam Integer size,
                                                 @RequestParam String keyword) {
        return userService.getUserInfos(page, size, keyword);
    }

}