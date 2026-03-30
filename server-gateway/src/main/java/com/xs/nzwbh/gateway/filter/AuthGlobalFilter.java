package com.xs.nzwbh.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String PEST_API = "/api/recognize";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        long start = System.currentTimeMillis();

        return chain.filter(exchange)
                .doOnSuccess(res -> {
                    long cost = System.currentTimeMillis() - start;

                    if (path.contains(PEST_API)) {
                        log.info("【虫害检测接口】耗时={}ms", cost);
                    }
                })
                .doOnError(e -> {
                    if (path.contains(PEST_API)) {
                        log.error("【虫害检测接口异常】", e);
                    }
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}