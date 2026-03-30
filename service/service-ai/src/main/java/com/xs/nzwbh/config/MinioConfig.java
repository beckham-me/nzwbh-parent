package com.xs.nzwbh.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint; // MinIO 服务端地址
    @Value("${minio.accessKey}")
    private String accessKey;  // 访问密钥（用户名）
    @Value("${minio.secretKey}")
    private String secretKey; // 秘密密钥（密码）

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}