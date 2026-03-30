package com.xs.nzwbh.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xs.nzwbh.model.entity.Admin;
import com.xs.nzwbh.model.dto.LoginRequest;

public interface AdminService extends IService<Admin> {

    
    boolean register(LoginRequest request);

    boolean login(LoginRequest request);


}
