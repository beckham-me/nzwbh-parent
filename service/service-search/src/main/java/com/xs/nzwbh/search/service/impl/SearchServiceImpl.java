package com.xs.nzwbh.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.crop.client.CropFeignClient;
import com.xs.nzwbh.model.entity.SearchHistory;
import com.xs.nzwbh.model.vo.SearchVo;
import com.xs.nzwbh.pest.client.CropPestFeignClient;
import com.xs.nzwbh.search.config.SearchProperties;
import com.xs.nzwbh.search.mapper.SearchHistoryMapper;
import com.xs.nzwbh.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements SearchService {

    private final SearchHistoryMapper searchHistoryMapper;
    private final CropFeignClient cropFeignClient;
    private final CropPestFeignClient cropPestFeignClient;
    private final ExecutorService searchExecutor;
    private final SearchProperties searchProperties;

    // 默认的搜索结果（当所有服务都不可用时返回）
    private static final SearchVo DEFAULT_SEARCH_VO = createDefaultSearchVo();

    public SearchServiceImpl(SearchHistoryMapper searchHistoryMapper,
                             CropFeignClient cropFeignClient,
                             CropPestFeignClient cropPestFeignClient,
                             ExecutorService searchExecutor,
                             SearchProperties searchProperties) {
        this.searchHistoryMapper = searchHistoryMapper;
        this.cropFeignClient = cropFeignClient;
        this.cropPestFeignClient = cropPestFeignClient;
        this.searchExecutor = searchExecutor;
        this.searchProperties = searchProperties;
    }

    @Override
    public Result<List<String>> getSearchSuggestions(String keyword) {
        long start = System.currentTimeMillis();
        try {
            // 并发调用两个 Feign 接口
            CompletableFuture<List<String>> cropFuture = CompletableFuture
                    .supplyAsync(() -> cropFeignClient.findSimilarCropNames(keyword), searchExecutor)
                    .orTimeout(searchProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        log.warn("作物服务调用失败: {}", ex.getMessage());
                        return null;
                    });

            CompletableFuture<List<String>> pestFuture = CompletableFuture
                    .supplyAsync(() -> cropPestFeignClient.findSimilarDiseaseAndPestNames(keyword), searchExecutor)
                    .orTimeout(searchProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        log.warn("病虫害服务调用失败: {}", ex.getMessage());
                        return null;
                    });

            // 等待两者完成
            CompletableFuture.allOf(cropFuture, pestFuture).join();

            // 合并结果
            Set<String> resultSet = new LinkedHashSet<>();
            Optional.ofNullable(cropFuture.join()).ifPresent(resultSet::addAll);
            Optional.ofNullable(pestFuture.join()).ifPresent(resultSet::addAll);

            if (resultSet.isEmpty()) {
                log.debug("未找到任何搜索建议，关键词: {}", keyword);
                return Result.fail();
            }

            return Result.ok(new ArrayList<>(resultSet));
        } finally {
            log.debug("getSearchSuggestions 耗时: {}ms", System.currentTimeMillis() - start);
        }
    }

    @Override
    public Result<List<String>> getLastlySearchHistory(Long userId) {
        long start = System.currentTimeMillis();
        try {
            Page<SearchHistory> page = new Page<>(1, searchProperties.getHistorySize());
            LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SearchHistory::getUserId, userId)
                    .eq(SearchHistory::getIsDeleted, 0)
                    .orderByDesc(SearchHistory::getUpdateTime);
            Page<SearchHistory> result = searchHistoryMapper.selectPage(page, wrapper);
            List<String> list = result.getRecords().stream()
                    .map(SearchHistory::getKeyword)
                    .collect(Collectors.toList());
            return Result.ok(list);
        } finally {
            log.debug("getLastlySearchHistory 耗时: {}ms", System.currentTimeMillis() - start);
        }
    }

    @Override
    public Result<List<String>> getLastlySearchHot() {
        long start = System.currentTimeMillis();
        try {
            Page<SearchHistory> page = new Page<>(1, searchProperties.getHotSize());
            LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SearchHistory::getIsHot, 1)
                    .orderByDesc(SearchHistory::getUpdateTime);
            Page<SearchHistory> result = searchHistoryMapper.selectPage(page, wrapper);
            List<String> list = result.getRecords().stream()
                    .map(SearchHistory::getSource)
                    .collect(Collectors.toList());
            return Result.ok(list);
        } finally {
            log.debug("getLastlySearchHot 耗时: {}ms", System.currentTimeMillis() - start);
        }
    }

    @Override
    public Result<SearchVo> getVerify(String keyword) {
        long start = System.currentTimeMillis();
        try {
            // 并发调用并设置超时
            CompletableFuture<SearchVo> cropFuture = CompletableFuture
                    .supplyAsync(() -> cropFeignClient.findMostSimilarName(keyword), searchExecutor)
                    .orTimeout(searchProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        log.warn("作物服务 verify 调用失败: {}", ex.getMessage());
                        return null;
                    });

            CompletableFuture<SearchVo> pestFuture = CompletableFuture
                    .supplyAsync(() -> cropPestFeignClient.findMostSimilarName(keyword), searchExecutor)
                    .orTimeout(searchProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        log.warn("病虫害服务 verify 调用失败: {}", ex.getMessage());
                        return null;
                    });

            // 等待结果
            CompletableFuture.allOf(cropFuture, pestFuture).join();

            SearchVo cropResult = cropFuture.join();
            SearchVo pestResult = pestFuture.join();

            // 如果两个都为空，返回默认值（友好提示）
            if (cropResult == null && pestResult == null) {
                log.warn("两个服务均未返回有效结果，关键词: {}", keyword);
                return Result.ok(DEFAULT_SEARCH_VO);
            }

            // 选择更相关的结果
            SearchVo finalResult;
            if (cropResult != null && pestResult != null) {
                finalResult = compareResults(keyword, cropResult, pestResult);
            } else if (cropResult != null) {
                finalResult = cropResult;
                finalResult.setType("crop");
            } else {
                finalResult = pestResult;
                finalResult.setType("pest");
            }

            return Result.ok(finalResult);
        } catch (Exception e) {
            log.error("搜索验证失败", e);
            return Result.ok(DEFAULT_SEARCH_VO);
        } finally {
            log.debug("getVerify 耗时: {}ms", System.currentTimeMillis() - start);
        }
    }

    /**
     * 比较两个结果的相似度，返回更相关的一个
     */
    private SearchVo compareResults(String keyword, SearchVo cropResult, SearchVo cropPestResult) {
        double cropSimilarity = calculateSimilarity(keyword, cropResult.getName());
        double cropPestSimilarity = calculateSimilarity(keyword, cropPestResult.getName());
        return cropSimilarity >= cropPestSimilarity ? cropResult : cropPestResult;
    }

    /**
     * 计算字符串相似度（基于前缀匹配比例）
     */
    private double calculateSimilarity(String keyword, String name) {
        if (keyword == null || name == null) {
            return 0.0;
        }
        int keywordLength = keyword.length();
        int nameLength = name.length();
        int maxLen = Math.max(keywordLength, nameLength);
        int commonChars = 0;
        for (int i = 0; i < Math.min(keywordLength, nameLength); i++) {
            if (keyword.charAt(i) == name.charAt(i)) {
                commonChars++;
            }
        }
        return (double) commonChars / maxLen;
    }

    /**
     * 创建一个默认的 SearchVo，表示“未找到”
     */
    private static SearchVo createDefaultSearchVo() {
        SearchVo defaultVo = new SearchVo();
        defaultVo.setId(-1L);
        defaultVo.setName("未找到相关结果");
        defaultVo.setType("default");

        return defaultVo;
    }
}