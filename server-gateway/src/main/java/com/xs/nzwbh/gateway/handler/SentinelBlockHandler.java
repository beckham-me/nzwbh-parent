package com.xs.nzwbh.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class SentinelBlockHandler implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        // 用于封装返回给前端的错误信息
        Map<String, Object> result = new HashMap<>();
        result.put("code", 429);
        result.put("msg", "请求被限流或服务降级，请稍后再试");
        // 构建响应：设置 HTTP 状态码为 429
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)// 设置响应内容类型：application/json
                .bodyValue(result);// 设置响应体：将封装的错误信息作为 JSON 返回
    }
}