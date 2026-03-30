package com.xs.nzwbh.user.controller;


import com.xs.nzwbh.common.login.Login;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.common.util.AuthContextHolder;
import com.xs.nzwbh.model.dto.FeedbackDTO;
import com.xs.nzwbh.user.client.UserFeignClient;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户反馈控制器
 */
@Slf4j
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 保存用户反馈
     *
     * @param feedback 反馈信息（需通过校验）
     * @return 统一返回结果
     */
    @Login
    @PostMapping("/save")
    public Result<?> saveFeedback(@Valid @RequestBody FeedbackDTO feedback) {
        // 1. 获取当前登录用户ID
        Long userId = AuthContextHolder.getUserId();
        if (userId == null) {
            log.warn("用户未登录或登录信息失效，无法提交反馈");
            return Result.failMessage("请先登录");
        }

        // 2. 参数校验（如已有 @Valid 注解，可依赖 DTO 中的校验，但也可以手动补充）
        if (feedback == null) {
            log.warn("反馈内容为空，userId: {}", userId);
            return Result.failMessage("反馈内容不能为空");
        }

        // 可选：对 feedback 的内容进一步校验，如 content 是否为空等（建议在 DTO 中通过注解完成）

        log.info("保存反馈，userId: {}, feedback: {}", userId, feedback);

        try {
            // 3. 调用远程服务保存反馈
            Result<?> result = userFeignClient.saveFeedback(userId, feedback);

            // 4. 校验远程调用结果
            if (result == null) {
                log.error("调用反馈服务返回结果为空，userId: {}", userId);
                return Result.failMessage("保存反馈失败，请稍后重试");
            }

            // 假设 Result 对象包含 code 字段，200 表示成功
            Integer code = result.getCode();
            if (code == null || code != 200) {
                log.warn("保存反馈失败，userId: {}, code: {}, message: {}", userId, code, result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "保存反馈失败";
                return Result.failMessage(errorMsg);
            }

            return result;
        } catch (Exception e) {
            log.error("调用反馈服务异常，userId: {}", userId, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}
