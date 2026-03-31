package com.xs.nzwbh.tools;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 调用YOLO微服务进行虫害识别
 */
@Component
public class PestDetectTool {

    private final WebClient webClient;

    @Value("${flask.url}")
    private String flaskUrl;

    public PestDetectTool(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 识别图片中的虫害
     *
     * @param imageUrl 图片URL
     * @return JSON识别结果
     */
    public String detect(String imageUrl) {

        return webClient.post()
                .uri(flaskUrl)               // 调用微服务
                .bodyValue(imageUrl)         // 传入图片URL
                .retrieve()                  // 执行请求
                .bodyToMono(String.class)    // 返回字符串
                .block();                    // 阻塞获取
    }
}
