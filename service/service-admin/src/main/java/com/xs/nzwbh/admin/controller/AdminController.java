package com.xs.nzwbh.admin.controller;


import com.xs.nzwbh.admin.service.AdminService;
import com.xs.nzwbh.model.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;


    @PostMapping("/login")
    public boolean login(@RequestBody LoginRequest request) {
        return adminService.login(request);

    }

    @PostMapping("/register")
    public boolean register(@RequestBody LoginRequest request) {
        return adminService.register(request);
    }
}


