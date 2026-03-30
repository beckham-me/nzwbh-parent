package com.xs.nzwbh.common.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

    @Value("${crop.pest.bloom-filter.name}")
    private String bloomFilterName; // 布隆过滤器在 Redis 中的唯一标识名

    @Value("${crop.pest.bloom-filter.expected-insertions}")
    private long expectedInsertions;  // 预期要插入的元素数量（影响位数组大小）

    @Value("${crop.pest.bloom-filter.false-probability}")
    private double falseProbability; // 允许的误判概率（如 0.01 表示 1% 误判率）

    @Bean
    public RBloomFilter<String> pestBloomFilter(RedissonClient redissonClient) {
        // 从 Redisson 获取指定名称的布隆过滤器对象（不存在则创建）
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(bloomFilterName);
        bloomFilter.tryInit(expectedInsertions, falseProbability);
        return bloomFilter;
    }
}
