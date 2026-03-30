package com.xs.nzwbh.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.entity.SearchHistory;
import com.xs.nzwbh.model.vo.SearchVo;

import java.util.List;

public interface SearchService extends IService<SearchHistory> {
    Result<List<String>> getSearchSuggestions(String keyword);

    Result<List<String>> getLastlySearchHistory(Long userId);

    Result<List<String>> getLastlySearchHot();

    Result<SearchVo> getVerify(String keyword);
}
