package com.xs.nzwbh.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.crop.client.CropFeignClient;
import com.xs.nzwbh.model.entity.SearchHistory;
import com.xs.nzwbh.model.entity.User;
import com.xs.nzwbh.model.mgrvo.SearchHistory1Vo;
import com.xs.nzwbh.model.vo.SearchHistoryVo;
import com.xs.nzwbh.model.vo.SearchVo;
import com.xs.nzwbh.pest.client.CropPestFeignClient;
import com.xs.nzwbh.search.mapper.SearchHistoryMapper;
import com.xs.nzwbh.search.service.SearchHistoryService;
import com.xs.nzwbh.user.client.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements SearchHistoryService {

    private final SearchHistoryMapper searchHistoryMapper;
    private final CropFeignClient cropFeignClient;
    private final CropPestFeignClient cropPestFeignClient;
    private final UserFeignClient userFeignClient;

    // 分页默认大小（用于历史记录列表）
    private static final int DEFAULT_HISTORY_PAGE_SIZE = 100;

    /**
     * 构造器注入
     */
    public SearchHistoryServiceImpl(SearchHistoryMapper searchHistoryMapper,
                                    CropFeignClient cropFeignClient,
                                    CropPestFeignClient cropPestFeignClient,
                                    UserFeignClient userFeignClient) {
        this.searchHistoryMapper = searchHistoryMapper;
        this.cropFeignClient = cropFeignClient;
        this.cropPestFeignClient = cropPestFeignClient;
        this.userFeignClient = userFeignClient;
    }

    @Override
    @Transactional
    public void addSearchHistory(String keyword, Long userId) {
        // 参数校验
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("添加搜索历史时关键词为空，用户ID: {}", userId);
            return;
        }
        if (userId == null) {
            log.warn("添加搜索历史时用户ID为空，关键词: {}", keyword);
            return;
        }

        log.info("【添加搜索记录】关键词: {}, 用户ID: {}", keyword, userId);

        // 查询是否存在相同的搜索记录
        LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SearchHistory::getKeyword, keyword)
                .eq(SearchHistory::getUserId, userId);

        SearchHistory searchHistory = searchHistoryMapper.selectOne(queryWrapper);

        if (searchHistory != null) {
            // 如果存在相同的记录，更新 searchCount
            searchHistory.setSearchCount(searchHistory.getSearchCount() + 1);
            searchHistory.setUpdateTime(new Date());

            // 判断 searchCount 是否大于 10，更新 isHot
            searchHistory.setIsHot(searchHistory.getSearchCount() >= 10 ? 1 : 0);

            searchHistoryMapper.updateById(searchHistory);
            log.info("更新搜索记录成功");
        } else {
            // 如果不存在相同的记录，插入新的记录
            SearchHistory newSearchHistory = new SearchHistory();
            newSearchHistory.setKeyword(keyword);
            newSearchHistory.setUserId(userId);
            newSearchHistory.setCreateTime(new Date());
            newSearchHistory.setUpdateTime(new Date());
            newSearchHistory.setIsDeleted(0);
            newSearchHistory.setSearchCount(1L); // 初始搜索计数为 1

            // 判断 searchCount 是否大于 10，更新 isHot
            newSearchHistory.setIsHot(newSearchHistory.getSearchCount() >= 10 ? 1 : 0);

            String source = "未知";
            String resultType = null;

            SearchVo cropVo = null;
            SearchVo pestVo = null;

            try {
                // 调用两个 Feign 客户端获取数据
                cropVo = cropFeignClient.findMostSimilarName(keyword);
            } catch (Exception e) {
                log.error("调用 cropFeignClient 远程服务异常", e);
            }

            try {
                pestVo = cropPestFeignClient.findMostSimilarName(keyword);
            } catch (Exception e) {
                log.error("调用 cropPestFeignClient 远程服务异常", e);
            }

            // 比较相似度
            if (cropVo != null && cropVo.getName() != null && pestVo != null && pestVo.getName() != null) {
                int cropSimilarity = calculateSimilarity(keyword, cropVo.getName());
                int pestSimilarity = calculateSimilarity(keyword, pestVo.getName());

                if (cropSimilarity <= pestSimilarity) {
                    source = cropVo.getName();
                    resultType = "农作物";
                } else {
                    source = pestVo.getName();
                    resultType = pestVo.getType();
                }
            } else if (cropVo != null && cropVo.getName() != null) {
                source = cropVo.getName();
                resultType = "农作物";
            } else if (pestVo != null && pestVo.getName() != null) {
                source = pestVo.getName();
                resultType = pestVo.getType();
            }

            newSearchHistory.setSource(source);
            newSearchHistory.setResultType(resultType);

            int rows = searchHistoryMapper.insert(newSearchHistory);
            log.info("插入历史记录是否成功：{}", rows > 0 ? "是" : "否");
        }
    }

    /**
     * 计算两个字符串的相似度（Levenshtein 距离）
     */
    private int calculateSimilarity(String keyword, String name) {
        LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();
        return levenshteinDistance.apply(keyword, name);
    }

    @Override
    @Transactional
    public void clearSearchHistory(Long userId) {
        // 参数校验
        if (userId == null) {
            log.warn("清空搜索记录时用户ID为空");
            return;
        }

        log.info("【清空搜索记录】用户ID: {}", userId);

        // 创建更新条件
        LambdaUpdateWrapper<SearchHistory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SearchHistory::getUserId, userId)
                .set(SearchHistory::getIsDeleted, 1);

        // 执行更新操作
        int rows = searchHistoryMapper.update(null, updateWrapper);
        log.info("更新操作影响了 {} 行", rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchHistoryVo> getSearchHistoryList(Long userId) {
        // 参数校验
        if (userId == null) {
            log.warn("获取搜索记录时用户ID为空");
            return Collections.emptyList();
        }

        log.info("【获取搜索记录】用户ID: {}", userId);

        // 分页查询，限制最大返回条数
        Page<SearchHistory> page = new Page<>(1, DEFAULT_HISTORY_PAGE_SIZE);
        LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getCreateTime);

        Page<SearchHistory> pageResult = searchHistoryMapper.selectPage(page, queryWrapper);

        // 转换为 VO
        return pageResult.getRecords().stream()
                .map(history -> {
                    SearchHistoryVo vo = new SearchHistoryVo();
                    BeanUtils.copyProperties(history, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // 管理员端：分页查询搜索历史（已使用分页，无需修改）
    @Override
    @Transactional(readOnly = true)
    public Result<Page<SearchHistory1Vo>> getSearchHistory(Integer page, Integer size, String keyword) {
        // 参数校验（可添加）
        if (page == null || page <= 0) page = 1;
        if (size == null || size <= 0) size = 10;

        // 查询 SearchHistory 分页数据
        Page<SearchHistory> searchHistoryPage = new Page<>(page, size);
        QueryWrapper<SearchHistory> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("keyword", keyword);
        }
        Page<SearchHistory> searchHistoryPageResult = searchHistoryMapper.selectPage(searchHistoryPage, queryWrapper);

        // 提取 userId 列表
        List<Long> userIds = searchHistoryPageResult.getRecords().stream()
                .map(SearchHistory::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 根据 userId 列表查询对应的 username
        Map<Long, String> userIdToUsernameMap;
        if (!userIds.isEmpty()) {
            List<User> users = userFeignClient.getUsersByIds(userIds); // 假设 userMapper 是用户表的 Mapper
            userIdToUsernameMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getNickname));
        } else {
            userIdToUsernameMap = new HashMap<>();
        }

        // 将 SearchHistory 转换为 SearchHistory1Vo
        Page<SearchHistory1Vo> searchHistoryVoPage = new Page<>();
        searchHistoryVoPage.setCurrent(searchHistoryPageResult.getCurrent());
        searchHistoryVoPage.setSize(searchHistoryPageResult.getSize());
        searchHistoryVoPage.setTotal(searchHistoryPageResult.getTotal());
        searchHistoryVoPage.setPages(searchHistoryPageResult.getPages());
        searchHistoryVoPage.setRecords(searchHistoryPageResult.getRecords().stream()
                .map(searchHistory -> convertToVo(searchHistory, userIdToUsernameMap))
                .collect(Collectors.toList()));

        return Result.ok(searchHistoryVoPage);
    }

    private SearchHistory1Vo convertToVo(SearchHistory searchHistory, Map<Long, String> userIdToUsernameMap) {
        SearchHistory1Vo vo = new SearchHistory1Vo();
        BeanUtils.copyProperties(searchHistory, vo);
        // 根据 userId 获取 username
        Long userId = searchHistory.getUserId();
        vo.setUsername(userIdToUsernameMap.getOrDefault(userId, ""));
        return vo;
    }

    @Override
    @Transactional
    public Result<Void> deleteSearchHistory(Long id) {
        // 参数校验
        if (id == null) {
            log.warn("删除搜索历史时ID为空");
            return Result.fail();
        }

        LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SearchHistory::getId, id);
        boolean success = searchHistoryMapper.delete(queryWrapper) > 0;
        return success ? Result.ok() : Result.fail();
    }
}