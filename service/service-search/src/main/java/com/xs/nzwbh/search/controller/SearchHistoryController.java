package com.xs.nzwbh.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.mgrvo.SearchHistory1Vo;
import com.xs.nzwbh.model.vo.SearchHistoryVo;
import com.xs.nzwbh.search.service.SearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/searchHistory")
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    @PostMapping("/add")
    public void addSearchHistory(@RequestParam("keyword") String keyword, @RequestParam("userId") Long userId) {
        searchHistoryService.addSearchHistory(keyword,userId);
    }
    @GetMapping("/list")
    public Result<List<SearchHistoryVo>> getSearchHistoryList(@RequestParam("userId") Long userId) {
        List<SearchHistoryVo> searchHistoryList = searchHistoryService.getSearchHistoryList(userId);
        return Result.ok(searchHistoryList);
    }
    @PostMapping("/clear")
    public void clearSearchHistory(@RequestParam("userId") Long userId) {
        searchHistoryService.clearSearchHistory(userId);
    }

    //管理员端
    @GetMapping
    public Result<Page<SearchHistory1Vo>> getSearchHistory(
                                                                                    @RequestParam Integer page,
                                                                                   @RequestParam Integer size,
                                                                                   @RequestParam String keyword) {
        return searchHistoryService.getSearchHistory(page, size, keyword);
    }


    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteSearchHistory(@PathVariable Long id) {
        return searchHistoryService.deleteSearchHistory(id);
    }
}
