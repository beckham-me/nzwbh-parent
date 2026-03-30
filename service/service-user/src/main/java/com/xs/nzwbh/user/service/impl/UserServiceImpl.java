package com.xs.nzwbh.user.service.impl;


import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.entity.User;
import com.xs.nzwbh.model.mgrvo.UserVo;
import com.xs.nzwbh.model.vo.UserLoginVo;
import com.xs.nzwbh.user.mapper.UserMapper;
import com.xs.nzwbh.user.service.UserService;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private WxMaService wxMaService;

    @Override
    public UserLoginVo getUserLoginInfo(Long userId) {
        // 通过继承的 getById 方法获取用户
        User user = this.getById(userId);
        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtils.copyProperties(user, userLoginVo);

        String phone = user.getPhone();
        boolean isBindPhone = StringUtils.hasText(phone);
        userLoginVo.setIsBindPhone(isBindPhone);
        return userLoginVo;
    }

    @Override
    public Long login(String code) {
        String openid = null;
        try {
            WxMaJscode2SessionResult sessionInfo =
                    wxMaService.getUserService().getSessionInfo(code);
            openid = sessionInfo.getOpenid();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }

        // 使用 LambdaQueryWrapper 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getWxOpenId, openid);
        User user = this.getOne(queryWrapper);

        if (user == null) {
            user = new User();
            user.setWxOpenId(openid);
            user.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            user.setNickname(String.valueOf(System.currentTimeMillis()));
            // 通过继承的 save 方法保存
            this.save(user);
        }

        return user.getId();
    }

    @Override
    public String getUserOpenId(Long userId) {
        // 通过继承的 getById 方法获取用户
        User user = this.getById(userId);
        return user.getWxOpenId();
    }

    @Override
    public List<User> getUsersByIds(List<Long> userIds) {
        // 参数校验：如果列表为空则直接返回空列表，避免无效查询
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        // 通过继承的 listByIds 方法批量查询
        return this.listByIds(userIds);
    }

    @Override
    public Result<Page<UserVo>> getUserInfos(Integer page, Integer size, String keyword) {
        // 参数校验与默认值处理
        int currentPage = (page == null || page <= 0) ? 1 : page;
        int pageSize = (size == null || size <= 0) ? 10 : size;

        Page<User> userPage = new Page<>(currentPage, pageSize);
        // 使用 LambdaQueryWrapper 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(User::getNickname, keyword);
        }
        // 通过继承的 page 方法分页查询
        Page<User> userPageResult = this.page(userPage, queryWrapper);

        // 将 User 转换为 UserVo
        Page<UserVo> userVoPage = new Page<>();
        userVoPage.setCurrent(userPageResult.getCurrent());
        userVoPage.setSize(userPageResult.getSize());
        userVoPage.setTotal(userPageResult.getTotal());
        userVoPage.setPages(userPageResult.getPages());
        userVoPage.setRecords(userPageResult.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList()));
        return Result.ok(userVoPage);
    }

    /**
     * 将 User 实体转换为 UserVo
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private UserVo convertToVo(User user) {
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}