package com.xs.nzwbh.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.xs.nzwbh.gateway.handler.SentinelBlockHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewaySentinelConfig {
    // 定义成员变量：注入熔断/限流触发后的自定义处理器（final 保证不可变）
    private final SentinelBlockHandler handler;
    // 通过构造器注入 SentinelBlockHandler 实例
    public GatewaySentinelConfig(SentinelBlockHandler handler) {
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        // 设置网关回调处理器：当触发限流或熔断时，使用自定义 handler 处理响应
        GatewayCallbackManager.setBlockHandler(handler);
    }
}