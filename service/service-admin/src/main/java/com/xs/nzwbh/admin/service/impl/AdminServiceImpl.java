package com.xs.nzwbh.admin.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.admin.mapper.AdminMapper;
import com.xs.nzwbh.admin.service.AdminService;
import com.xs.nzwbh.model.dto.LoginRequest;
import com.xs.nzwbh.model.entity.Admin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    // 密码强度常量
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * 构造器注入
     */
    public AdminServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public boolean login(LoginRequest request) {
        // 参数校验
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            log.warn("登录请求参数不完整");
            return false;
        }

        String username = request.getUsername().trim();
        String password = request.getPassword();

        // 查询用户
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, username)
                .eq(Admin::getIsDeleted, 0);
        Admin admin = adminMapper.selectOne(queryWrapper);

        if (admin == null) {
            log.warn("登录失败，用户不存在: {}", username);
            return false;
        }

        // 密码验证
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            log.warn("登录失败，密码错误: {}", username);
            return false;
        }

        log.info("登录成功: {}", username);
        return true;
    }

    @Override
    @Transactional
    public boolean register(LoginRequest request) {
        // 参数校验
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            log.warn("注册请求参数不完整");
            return false;
        }

        String username = request.getUsername().trim();
        String password = request.getPassword();

        // 长度校验
        if (username.length() < MIN_USERNAME_LENGTH) {
            log.warn("注册失败，用户名长度不足: {}", username);
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            log.warn("注册失败，密码长度不足");
            return false;
        }

        // 空格校验
        if (username.contains(" ") || password.contains(" ")) {
            log.warn("注册失败，用户名或密码包含空格");
            return false;
        }

        // 唯一性校验
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, username)
                .eq(Admin::getIsDeleted, 0);
        if (count(queryWrapper) > 0) {
            log.warn("注册失败，用户名已存在: {}", username);
            return false;
        }

        try {
            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password)); // 加密存储
            admin.setCreateTime(new Date());
            admin.setUpdateTime(new Date());
            admin.setIsDeleted(0);

            boolean success = adminMapper.insert(admin) > 0;
            if (success) {
                log.info("注册成功: {}", username);
                return true;
            } else {
                log.error("注册失败，插入数据库无影响");
                return false;
            }
        } catch (Exception e) {
            log.error("注册异常: {}", e.getMessage(), e);
            return false;
        }
    }
}

