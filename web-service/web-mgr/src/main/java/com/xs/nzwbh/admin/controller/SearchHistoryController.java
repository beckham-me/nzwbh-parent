package com.xs.nzwbh.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.mgrvo.SearchHistory1Vo;
import com.xs.nzwbh.search.client.SearchFeignClient;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索历史管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/searchHistory")
public class SearchHistoryController {

    @Autowired
    private SearchFeignClient searchFeignClient;

    /**
     * 分页获取搜索历史列表
     *
     * @param page    当前页码（默认1，最小1）
     * @param size    每页条数（默认10，最小1）
     * @param keyword 搜索关键词（可选）
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<SearchHistory1Vo>> getSearchHistory(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String keyword) {
        log.info("获取搜索历史列表，参数: page={}, size={}, keyword={}", page, size, keyword);

        try {
            Result<Page<SearchHistory1Vo>> result = searchFeignClient.getSearchHistory(page, size, keyword);
            if (result == null) {
                log.error("获取搜索历史远程服务返回结果为空");
                return Result.failMessage("获取搜索历史失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("获取搜索历史失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取搜索历史失败";
                return Result.failMessage(errorMsg);
            }
            Page<SearchHistory1Vo> data = result.getData();
            if (data == null) {
                log.warn("获取搜索历史返回数据为空");
                return Result.ok(new Page<>()); // 返回空分页对象
            }
            return Result.ok(data);
        } catch (Exception e) {
            log.error("获取搜索历史异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 删除搜索历史记录
     *
     * @param id 搜索历史ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteSearchHistory(@PathVariable Long id) {
        log.info("删除搜索历史，参数: id={}", id);
        if (id == null || id <= 0) {
            log.warn("删除搜索历史时ID无效: {}", id);
            return Result.failMessage("搜索历史ID无效");
        }
        try {
            Result<Void> result = searchFeignClient.deleteSearchHistory(id);
            if (result == null) {
                log.error("删除搜索历史远程服务返回结果为空，id={}", id);
                return Result.failMessage("删除搜索历史失败，请稍后重试");
            }
            if (result.getCode() == null || result.getCode().intValue() != 200) {
                log.warn("删除搜索历史失败，id={}, code: {}, message: {}", id, result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "删除搜索历史失败";
                return Result.failMessage(errorMsg);
            }
            return Result.ok("搜索历史删除成功");
        } catch (Exception e) {
            log.error("删除搜索历史异常，id={}", id, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}
