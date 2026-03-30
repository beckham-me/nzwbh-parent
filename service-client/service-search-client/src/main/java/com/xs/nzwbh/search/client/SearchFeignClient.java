package com.xs.nzwbh.search.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.mgrvo.SearchHistory1Vo;
import com.xs.nzwbh.model.vo.SearchHistoryVo;
import com.xs.nzwbh.model.vo.SearchVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-search")
public interface SearchFeignClient {
    @GetMapping("/search/suggestions")
    public Result<List<String>> getSearchSuggestions(@RequestParam("keyword") String keyword);
    @PostMapping("/searchHistory/add")
    public void addSearchHistory(@RequestParam("keyword") String keyword,@RequestParam("userId") Long userId);
    @GetMapping("/searchHistory/list")
    public Result<List<SearchHistoryVo>> getSearchHistoryList(@RequestParam("userId") Long userId);
    @GetMapping("/search/lastlyHistory")
    public Result<List<String>> getLastlySearchHistory(@RequestParam("userId") Long userId);
    @GetMapping("/search/lastlyHot")
    public Result<List<String>> getLastlySearchHot();
    @PostMapping("/searchHistory/clear")
    public void clearSearchHistory(@RequestParam("userId") Long userId);
    @GetMapping("/search/verify")
    public Result<SearchVo> getVerify(@RequestParam("keyword") String keyword);

    @GetMapping("/searchHistory")
    public Result<Page<SearchHistory1Vo>> getSearchHistory(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam String keyword);
    @DeleteMapping("/searchHistory/delete/{id}")
    public Result<Void> deleteSearchHistory(@PathVariable Long id);
}
