package com.xs.nzwbh.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class GatewayFlowConfig {

    @PostConstruct
    public void init() {
        //用于存储网关限流规则（去重）
        Set<GatewayFlowRule> rules = new HashSet<>();
        //创建网关限流规则对象，指定路由 ID 为 "api_route"
        GatewayFlowRule rule = new GatewayFlowRule("api_route")
                .setCount(500) // QPS 500
                .setIntervalSec(1) // 设置统计时间窗口：1 秒
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER); // 令牌桶

        rules.add(rule);
        // 加载规则：将配置好的规则注册到 Sentinel 网关规则管理器
        GatewayRuleManager.loadRules(rules);
    }
}