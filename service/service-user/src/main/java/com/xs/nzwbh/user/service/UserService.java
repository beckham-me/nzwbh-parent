package com.xs.nzwbh.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.entity.User;
import com.xs.nzwbh.model.mgrvo.UserVo;
import com.xs.nzwbh.model.vo.UserLoginVo;

import java.util.List;


public interface UserService extends IService<User> {
    UserLoginVo getUserLoginInfo(Long userId);
    Long login(String code);
    String getUserOpenId(Long userId);

    Result<Page<UserVo>> getUserInfos(Integer page, Integer size, String keyword);

    List<User> getUsersByIds(List<Long> userIds);
}