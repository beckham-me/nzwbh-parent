package com.xs.nzwbh;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xs.nzwbh.mapper")
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients
public class ServiceAiApplication {
    public static void main(String[] args) {

        SpringApplication.run(ServiceAiApplication.class, args);
    }
}
