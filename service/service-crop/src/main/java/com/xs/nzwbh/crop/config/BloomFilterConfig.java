package com.xs.nzwbh.crop.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

    @Value("${crop.bloom-filter.name}")
    private String bloomFilterName;

    @Value("${crop.bloom-filter.expected-insertions}")
    private long expectedInsertions;

    @Value("${crop.bloom-filter.false-probability}")
    private double falseProbability;

    @Bean
    public RBloomFilter<String> cropBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(bloomFilterName);
        bloomFilter.tryInit(expectedInsertions, falseProbability);
        return bloomFilter;
    }
}
