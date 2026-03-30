package com.xs.nzwbh.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.entity.SearchHistory;
import com.xs.nzwbh.model.mgrvo.SearchHistory1Vo;
import com.xs.nzwbh.model.vo.SearchHistoryVo;

import java.util.List;

public interface SearchHistoryService extends IService<SearchHistory> {

    List<SearchHistoryVo> getSearchHistoryList(Long userId);

    void addSearchHistory(String keyword, Long userId);

    void clearSearchHistory(Long userId);

    Result<Page<SearchHistory1Vo>> getSearchHistory(Integer page, Integer size, String keyword);

    Result<Void> deleteSearchHistory(Long id);
}
