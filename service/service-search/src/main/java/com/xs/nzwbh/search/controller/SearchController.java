package com.xs.nzwbh.search.controller;


import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.vo.SearchVo;
import com.xs.nzwbh.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private SearchService searchService;
    @GetMapping("/suggestions")
    public Result<List<String>> getSearchSuggestions(@RequestParam("keyword") String keyword) {
        return searchService.getSearchSuggestions(keyword);
    }
    @GetMapping("/lastlyHistory")
    public Result<List<String>> getLastlySearchHistory(@RequestParam("userId") Long userId) {
        log.info("【获取搜索记录】用户ID: {}", userId);
        return searchService.getLastlySearchHistory(userId);
    }
    @GetMapping("/lastlyHot")
     public Result<List<String>> getLastlySearchHot() {
        return searchService.getLastlySearchHot();
    }
    @GetMapping("/verify")
    public Result<SearchVo> getVerify(@RequestParam("keyword") String keyword) {
        return searchService.getVerify(keyword);
    }
}
