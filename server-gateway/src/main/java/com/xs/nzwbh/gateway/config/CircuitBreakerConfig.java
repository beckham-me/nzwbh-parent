package com.xs.nzwbh.gateway.config;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
// 熔断器配置类
public class CircuitBreakerConfig {
    // 标记初始化方法，在 Bean 创建并注入依赖后自动执行
    @PostConstruct
    public void init() {
        // 用于存储所有熔断规则
        List<DegradeRule> rules = new ArrayList<>();
        // 定义一条熔断规则
        DegradeRule rule = new DegradeRule();
        // 指定对哪个 API 接口生效
        rule.setResource("/api/recognize");

        // 异常比例熔断
        // 设置熔断策略：按异常比例触发熔断
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);

        rule.setCount(0.5);               // 设置阈值：异常比例达到 50% 时触发熔断
        rule.setTimeWindow(10);           // 设置熔断时长：触发后熔断 10 秒
        rule.setMinRequestAmount(20);     // 设置最小请求数：统计周期内至少 20 个请求才进行熔断判断
        rule.setStatIntervalMs(10000);    // 设置统计周期：10 秒（10000毫秒）为一个统计窗口

        rules.add(rule);
        // 加载规则：将配置好的规则注册到 Sentinel 熔断管理器
        DegradeRuleManager.loadRules(rules);
    }
}