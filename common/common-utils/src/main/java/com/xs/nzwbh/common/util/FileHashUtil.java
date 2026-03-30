package com.xs.nzwbh.common.util;


import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;

public class FileHashUtil {

    public static String getFileMd5(MultipartFile file) {
        try(InputStream is = file.getInputStream()) {
            // 获取 MD5 消息摘要算法实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[4096];
            int len;

            while ((len = is.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            // 读取文件字节数组并计算 MD5 摘要，返回 16 字节数组
            byte[] digest = md.digest(file.getBytes());
            //用于拼接十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("MD5计算失败");
        }
    }
}
