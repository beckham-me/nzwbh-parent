package com.xs.nzwbh.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class SearchExecutorConfig {

    @Bean
    public ExecutorService searchExecutor() {

        // Executors.newFixedThreadPool: 创建一个固定大小的线程池
        // 参数1: 核心线程数 = 2，线程池始终保持 2 个活跃线程
        // 参数2: 自定义 ThreadFactory，用于创建具有特定名称和属性的线程
        return Executors.newFixedThreadPool(2, new ThreadFactory() {

            // AtomicInteger: 线程安全的整数计数器
            // 用于给线程编号，确保多线程环境下编号唯一且自增安全
            // 初始值为 1，每创建一个线程自动加 1
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "search-worker-" + threadNumber.getAndIncrement());

                // setDaemon(true): 设置为守护线程（后台线程）
                // 守护线程特点: 当所有非守护线程结束时，JVM 会自动退出，不会等待守护线程
                t.setDaemon(true);
                return t;
            }
        });
    }
}