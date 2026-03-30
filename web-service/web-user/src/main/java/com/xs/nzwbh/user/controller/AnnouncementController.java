package com.xs.nzwbh.user.controller;

import com.xs.nzwbh.announcement.client.AnnouncementFeignClient;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.vo.AnnouncementVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementFeignClient announcementFeignClient;

    /**
     * 获取最新公告
     * @return 统一返回结果，包含最新公告信息
     */
    @GetMapping("/last")
    public Result<AnnouncementVo> getLastAnnouncement() {
        log.info("接收到获取最新公告请求");

        try {
            // 调用远程服务
            Result<AnnouncementVo> result = announcementFeignClient.getLatestAnnouncement();

            // 校验返回结果是否为 null（防止 NPE）
            if (result == null) {
                log.warn("调用公告服务返回结果为空");
                return Result.failMessage("获取最新公告失败，请稍后重试");
            }

            // 判断业务是否成功（code 为 200 表示成功）
            Integer code = result.getCode();
            if (code == null || code != 200) {
                log.warn("获取最新公告失败，code: {}, message: {}", code, result.getMessage());
                // 优先使用远程返回的错误信息，否则使用默认提示
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取最新公告失败";
                return Result.failMessage(errorMsg);
            }

            // 成功返回结果
            return result;

        } catch (Exception e) {
            log.error("调用公告服务异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}