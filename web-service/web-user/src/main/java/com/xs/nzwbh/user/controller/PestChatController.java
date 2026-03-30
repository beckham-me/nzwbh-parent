package com.xs.nzwbh.user.controller;


import com.xs.nzwbh.aillm.client.AillmFeignClient;
import com.xs.nzwbh.common.login.Login;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.common.util.AuthContextHolder;
import com.xs.nzwbh.model.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 虫害智能问答控制器
 */
@Slf4j
@Tag(name = "虫害智能问答接口")
@RestController
@RequestMapping("/api/pest/chat")
public class PestChatController {

    @Autowired
    private AillmFeignClient aillmFeignClient;

    @Value("${upload.image.max-size:5242880}")      // 默认5MB
    private long maxImageSize;

    /**
     * 智能问答（支持纯文本或图片+文本）
     *
     * @param question 用户问题（必填）
     * @param file     图片文件（可选）
     * @return 问答结果
     */
    @Login
    @Operation(summary = "智能问答")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ChatResponse> chat(
            @RequestPart(value = "question") @NotBlank(message = "问题不能为空") String question,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Long userId = AuthContextHolder.getUserId();
        if (userId == null) {
            log.warn("未登录用户尝试智能问答");
            return Result.failMessage("请先登录");
        }

        log.info("用户 {} 发起智能问答，问题: {}, 是否有图片: {}", userId, question, file != null);

        // 如果有图片，进行文件校验
        if (file != null && !file.isEmpty()) {
            // 文件大小校验
            long size = file.getSize();
            if (size > maxImageSize) {
                log.warn("用户 {} 上传图片过大: {} bytes", userId, size);
                return Result.failMessage("图片文件过大，请上传小于 " + (maxImageSize / 1024 / 1024) + "MB 的图片");
            }
            // 文件类型校验
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("用户 {} 上传非图片文件: {}", userId, contentType);
                return Result.failMessage("只允许上传图片文件（jpg、png等）");
            }
        }

        try {
            // 调用远程AI服务
            ChatResponse response = aillmFeignClient.chat(question, file);
            if (response == null) {
                log.error("AI服务返回结果为空，userId: {}", userId);
                return Result.failMessage("问答服务异常，请稍后重试");
            }
            // 记录问答历史
            return Result.ok(response);
        } catch (Exception e) {
            log.error("智能问答异常，userId: {}", userId, e);
            return Result.failMessage("问答失败，请稍后重试");
        }
    }

}
