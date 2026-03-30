package com.xs.nzwbh.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "search")
public class SearchProperties {
    /**
     * 搜索建议最大条数
     */
    private int suggestionSize = 10;

    /**
     * 最近搜索历史记录数
     */
    private int historySize = 6;

    /**
     * 热门搜索记录数
     */
    private int hotSize = 6;

    /**
     * Feign 调用超时时间（毫秒）
     */
    private long timeout = 3000L;
}