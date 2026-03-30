package com.xs.nzwbh.user.controller;

import com.xs.nzwbh.ai.client.AiFeignClient;
import com.xs.nzwbh.common.login.Login;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.common.util.AuthContextHolder;
import com.xs.nzwbh.model.vo.RecognizeResultVO;
import com.xs.nzwbh.model.vo.RecognizeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 病虫害识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/recognize")
public class RecognizeController {

    @Autowired
    private AiFeignClient aiFeignClient;

    @Value("${upload.image.max-size:5242880}") // 默认5MB
    private long maxImageSize;

    @Value("${upload.video.max-size:524288000}") // 默认500MB
    private long maxVideoSize;

    /**
     * 识别图片中的病虫害
     *
     * @param file 上传的图片文件
     * @return 识别结果
     */
    @Login
    @PostMapping("/image")
    public Result<RecognizeResultVO> recognizeImage(@RequestPart("file") MultipartFile file) {
        try {
            // 1. 文件为空校验
            if (file == null || file.isEmpty()) {
                log.warn("上传文件为空");
                return Result.failMessage("请选择图片文件");
            }

            // 2. 文件大小校验
            long size = file.getSize();
            if (size > maxImageSize) {
                log.warn("图片文件过大，size: {} bytes，最大限制: {} bytes", size, maxImageSize);
                return Result.failMessage("图片文件过大，请上传小于 " + (maxImageSize / 1024 / 1024) + "MB 的图片");
            }

            // 3. 文件类型校验
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("非图片文件，contentType: {}", contentType);
                return Result.failMessage("只允许上传图片文件（jpg、png等）");
            }

            // 4. 获取当前登录用户ID
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.warn("用户未登录或登录信息失效");
                return Result.failMessage("请先登录");
            }

            long startTime = System.currentTimeMillis();
            log.info("开始识别图片，userId: {}, fileName: {}, size: {} bytes", userId, file.getOriginalFilename(), size);

            // 5. 调用远程识别服务
            Result<RecognizeResultVO> remoteResult = aiFeignClient.recognizeImage(file, userId);
            if (remoteResult == null) {
                log.error("远程服务返回结果为空，userId: {}", userId);
                return Result.failMessage("识别服务异常，请稍后重试");
            }

            if (remoteResult.getCode() == null || remoteResult.getCode() != 200) {
                log.warn("远程识别失败，userId: {}, code: {}, message: {}", userId, remoteResult.getCode(), remoteResult.getMessage());
                String errorMsg = remoteResult.getMessage() != null ? remoteResult.getMessage() : "图片识别失败";
                return Result.failMessage(errorMsg);
            }

            RecognizeResultVO data = remoteResult.getData();
            if (data == null) {
                log.warn("远程识别返回数据为空，userId: {}", userId);
                return Result.failMessage("识别结果为空");
            }

            long endTime = System.currentTimeMillis();
            log.info("图片识别完成，userId: {}, 耗时: {}ms, 结果: {}", userId, (endTime - startTime), data);
            return Result.ok(data);

        } catch (Exception e) {
            log.error("图片识别异常", e);
            return Result.failMessage("图片识别失败，请稍后重试");
        }
    }

    /**
     * 识别视频中的病虫害
     *
     * @param file 上传的视频文件
     * @return 识别结果
     */
    @Login
    @PostMapping("/video")
    public Result<RecognizeResultVO> recognizeVideo(@RequestPart("file") MultipartFile file) {
        try {
            // 1. 文件为空校验
            if (file == null || file.isEmpty()) {
                log.warn("上传文件为空");
                return Result.failMessage("请选择视频文件");
            }

            // 2. 文件大小校验
            long size = file.getSize();
            if (size > maxVideoSize) {
                log.warn("视频文件过大，size: {} bytes，最大限制: {} bytes", size, maxVideoSize);
                return Result.failMessage("视频文件过大，请上传小于 " + (maxVideoSize / 1024 / 1024) + "MB 的视频");
            }

            // 3. 文件类型校验
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                log.warn("非视频文件，contentType: {}", contentType);
                return Result.failMessage("只允许上传视频文件");
            }

            // 4. 获取当前登录用户ID
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.warn("用户未登录或登录信息失效");
                return Result.failMessage("请先登录");
            }

            long startTime = System.currentTimeMillis();
            log.info("开始识别视频，userId: {}, fileName: {}, size: {} bytes", userId, file.getOriginalFilename(), size);

            // 5. 调用远程识别服务
            Result<RecognizeResultVO> remoteResult = aiFeignClient.recognizeVideo(file, userId);
            if (remoteResult == null) {
                log.error("远程服务返回结果为空，userId: {}", userId);
                return Result.failMessage("识别服务异常，请稍后重试");
            }

            if (remoteResult.getCode() == null || remoteResult.getCode() != 200) {
                log.warn("远程识别失败，userId: {}, code: {}, message: {}", userId, remoteResult.getCode(), remoteResult.getMessage());
                String errorMsg = remoteResult.getMessage() != null ? remoteResult.getMessage() : "视频识别失败";
                return Result.failMessage(errorMsg);
            }

            RecognizeResultVO data = remoteResult.getData();
            if (data == null) {
                log.warn("远程识别返回数据为空，userId: {}", userId);
                return Result.failMessage("识别结果为空");
            }

            long endTime = System.currentTimeMillis();
            log.info("视频识别完成，userId: {}, 耗时: {}ms, 结果: {}", userId, (endTime - startTime), data);
            return Result.ok(data);

        } catch (Exception e) {
            log.error("视频识别异常", e);
            return Result.failMessage("视频识别失败，请稍后重试");
        }
    }

    /**
     * 获取当前用户的识别记录列表
     *
     * @return 识别记录列表
     */
    @Login
    @GetMapping("/records")
    public Result<List<RecognizeVO>> getUserRecords() {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.warn("用户未登录或登录信息失效");
                return Result.failMessage("请先登录");
            }

            log.info("获取用户识别记录，userId: {}", userId);

            Result<List<RecognizeVO>> remoteResult = aiFeignClient.getUserRecords(userId);
            if (remoteResult == null) {
                log.error("远程服务返回结果为空，userId: {}", userId);
                return Result.failMessage("获取记录失败，请稍后重试");
            }

            if (remoteResult.getCode() == null || remoteResult.getCode() != 200) {
                log.warn("获取记录失败，userId: {}, code: {}, message: {}", userId, remoteResult.getCode(), remoteResult.getMessage());
                String errorMsg = remoteResult.getMessage() != null ? remoteResult.getMessage() : "获取记录失败";
                return Result.failMessage(errorMsg);
            }

            List<RecognizeVO> data = remoteResult.getData();
            if (data == null) {
                log.warn("获取记录返回数据为空，userId: {}", userId);
                return Result.ok(List.of()); // 返回空列表而不是null
            }
            return Result.ok(data);

        } catch (Exception e) {
            log.error("获取用户识别记录异常", e);
            return Result.failMessage("获取记录失败，请稍后重试");
        }
    }

    /**
     * 清空当前用户的识别记录
     *
     * @return 操作结果
     */
    @Login
    @DeleteMapping("/records")
    public Result<?> clearUserRecords() {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.warn("用户未登录或登录信息失效");
                return Result.failMessage("请先登录");
            }

            log.info("清空用户识别记录，userId: {}", userId);

            Result<?> remoteResult = aiFeignClient.clearUserRecords(userId);
            if (remoteResult == null) {
                log.error("远程服务返回结果为空，userId: {}", userId);
                return Result.failMessage("清空记录失败，请稍后重试");
            }

            if (remoteResult.getCode() == null || remoteResult.getCode() != 200) {
                log.warn("清空记录失败，userId: {}, code: {}, message: {}", userId, remoteResult.getCode(), remoteResult.getMessage());
                String errorMsg = remoteResult.getMessage() != null ? remoteResult.getMessage() : "清空记录失败";
                return Result.failMessage(errorMsg);
            }

            return Result.ok("清空成功");

        } catch (Exception e) {
            log.error("清空用户识别记录异常", e);
            return Result.failMessage("清空记录失败，请稍后重试");
        }
    }
}
