package com.xs.nzwbh.common.util;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

@Component
public class RestTemplateUtil {

    private final RestTemplate restTemplate;

    @Value("${python.recognize.image.url}")
    private String imageRecognizeUrl;

    @Value("${python.recognize.video.url}")
    private String videoRecognizeUrl;

    public RestTemplateUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 通用 POST 请求方法
     */
    private Map<String, Object> postForRecognition(String url, Map<String, Object> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }

    /**
     * 图像识别调用
     */
    public Map<String, Object> recognizeImage(Map<String, Object> request) {
        return postForRecognition(imageRecognizeUrl, request);
    }

    /**
     * 视频识别调用
     */
    public Map<String, Object> recognizeVideo(Map<String, Object> request) {
        return postForRecognition(videoRecognizeUrl, request);
    }
}
