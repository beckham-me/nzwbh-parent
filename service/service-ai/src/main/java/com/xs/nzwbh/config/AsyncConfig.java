package com.xs.nzwbh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("recognizeExecutor")
    public ThreadPoolTaskExecutor recognizeExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数（根据CPU/GPU）
        executor.setCorePoolSize(3);

        // 最大线程数（防止GPU爆）
        executor.setMaxPoolSize(4);

        // 队列容量（MQ缓冲）
        executor.setQueueCapacity(100);

        // 线程名前缀
        executor.setThreadNamePrefix("recognize-");

        // 拒绝策略（关键！）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        return executor;
    }
}
