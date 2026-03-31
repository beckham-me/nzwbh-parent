package com.xs.nzwbh.util;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
public class MinioUtil {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioUtil(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传文件
     */
    public String upload(MultipartFile file) {
        try {

            //判断bucket是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            // 生成唯一文件名（防止覆盖）
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回访问URL
            return endpoint + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("上传MinIO失败", e);
        }
    }
}