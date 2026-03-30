package com.xs.nzwbh.admin.client;


import com.xs.nzwbh.model.dto.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "service-admin")
public interface AdminFeignClient {
    @PostMapping("/admin/login")
    public boolean login(@RequestBody LoginRequest request);
    @PostMapping("/admin/register")
    public boolean register(@RequestBody LoginRequest request);
}
