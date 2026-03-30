package com.xs.nzwbh.user.controller;

import com.xs.nzwbh.common.login.Login;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.common.util.AuthContextHolder;
import com.xs.nzwbh.model.vo.SearchHistoryVo;
import com.xs.nzwbh.model.vo.SearchVo;
import com.xs.nzwbh.search.client.SearchFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 搜索控制器
 */
/**
 * 搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchFeignClient searchFeignClient;

    /**
     * 获取搜索关键词建议
     *
     * @param keyword 输入的关键词
     * @return 建议列表
     */
    @GetMapping("/suggestions")
    public Result<List<String>> getSearchSuggestions(@RequestParam("keyword") String keyword) {
        log.info("获取搜索建议，keyword: {}", keyword);
        try {
            // 参数校验
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("搜索关键词为空");
                return Result.ok(List.of()); // 返回空列表
            }

            Result<List<String>> result = searchFeignClient.getSearchSuggestions(keyword);
            if (result == null) {
                log.error("搜索建议远程服务返回结果为空，keyword: {}", keyword);
                return Result.failMessage("获取搜索建议失败，请稍后重试");
            }

            if (result.getCode() == null || result.getCode() != 200) {
                log.warn("获取搜索建议失败，keyword: {}, code: {}, message: {}", keyword, result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取搜索建议失败";
                return Result.failMessage(errorMsg);
            }

            List<String> data = result.getData();
            return Result.ok(data != null ? data : List.of());

        } catch (Exception e) {
            log.error("获取搜索建议异常，keyword: {}", keyword, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 获取用户最近搜索记录
     *
     * @return 最近搜索关键词列表
     */
    @Login
    @GetMapping("/lastlyHistory")
    public Result<List<String>> getLastlySearchHistory() {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.warn("用户未登录，无法获取搜索记录");
                return Result.failMessage("请先登录");
            }
            log.info("获取用户最近搜索记录，userId: {}", userId);

            Result<List<String>> result = searchFeignClient.getLastlySearchHistory(userId);
            if (result == null) {
                log.error("最近搜索记录远程服务返回结果为空，userId: {}", userId);
                return Result.failMessage("获取搜索记录失败，请稍后重试");
            }

            if (result.getCode() == null || result.getCode() != 200) {
                log.warn("获取最近搜索记录失败，userId: {}, code: {}, message: {}", userId, result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取搜索记录失败";
                return Result.failMessage(errorMsg);
            }

            List<String> data = result.getData();
            return Result.ok(data != null ? data : List.of());

        } catch (Exception e) {
            log.error("获取最近搜索记录异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 获取热门搜索关键词
     *
     * @return 热门关键词列表
     */
    @GetMapping("/lastlyHot")
    public Result<List<String>> getLastlySearchHot() {
        log.info("获取热门搜索关键词");
        try {
            Result<List<String>> result = searchFeignClient.getLastlySearchHot();
            if (result == null) {
                log.error("热门搜索远程服务返回结果为空");
                return Result.failMessage("获取热门搜索失败，请稍后重试");
            }

            if (result.getCode() == null || result.getCode() != 200) {
                log.warn("获取热门搜索失败，code: {}, message: {}", result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取热门搜索失败";
                return Result.failMessage(errorMsg);
            }

            List<String> data = result.getData();
            return Result.ok(data != null ? data : List.of());

        } catch (Exception e) {
            log.error("获取热门搜索异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 获取当前用户的完整搜索历史（带时间戳）
     *
     * @return 搜索历史列表
     */
    @Login
    @GetMapping("/userHistory")
    public Result<List<SearchHistoryVo>> getSearchHistoryList() {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.warn("用户未登录，无法获取搜索历史");
                return Result.failMessage("请先登录");
            }
            log.info("获取用户搜索历史，userId: {}", userId);

            Result<List<SearchHistoryVo>> result = searchFeignClient.getSearchHistoryList(userId);
            if (result == null) {
                log.error("搜索历史远程服务返回结果为空，userId: {}", userId);
                return Result.failMessage("获取搜索历史失败，请稍后重试");
            }

            if (result.getCode() == null || result.getCode() != 200) {
                log.warn("获取搜索历史失败，userId: {}, code: {}, message: {}", userId, result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "获取搜索历史失败";
                return Result.failMessage(errorMsg);
            }

            List<SearchHistoryVo> data = result.getData();
            return Result.ok(data != null ? data : List.of());

        } catch (Exception e) {
            log.error("获取搜索历史异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 添加搜索记录
     *
     * @param keyword 搜索关键词
     * @return 操作结果
     */
    @Login
    @PostMapping("/addHistory")
    public Result<Void> addSearchHistory(@RequestParam("keyword") String keyword) {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.warn("用户未登录，无法添加搜索记录");
                return Result.failMessage("请先登录");
            }
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("添加搜索记录关键词为空，userId: {}", userId);
                return Result.failMessage("关键词不能为空");
            }

            log.info("添加搜索记录，keyword: {}, userId: {}", keyword, userId);
            // 调用远程服务（可能是 void 或 Result，只关心是否抛异常）
            searchFeignClient.addSearchHistory(keyword, userId);
            return Result.ok();

        } catch (Exception e) {
            log.error("添加搜索记录异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 清空当前用户的搜索历史
     *
     * @return 操作结果
     */
    @Login
    @PostMapping("/clear")
    public Result<Void> clearSearchHistory() {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.warn("用户未登录，无法清空搜索历史");
                return Result.failMessage("请先登录");
            }
            log.info("清空搜索历史，userId: {}", userId);
            // 调用远程服务（可能是 void 或 Result，只关心是否抛异常）
            searchFeignClient.clearSearchHistory(userId);
            return Result.ok();

        } catch (Exception e) {
            log.error("清空搜索历史异常", e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }

    /**
     * 验证关键词（例如：是否为有效虫害/作物名称）
     *
     * @param keyword 关键词
     * @return 验证结果
     */
    @GetMapping("/verify")
    public Result<SearchVo> verify(@RequestParam("keyword") String keyword) {
        log.info("验证关键词，keyword: {}", keyword);
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("验证关键词为空");
                return Result.failMessage("关键词不能为空");
            }

            Result<SearchVo> result = searchFeignClient.getVerify(keyword);
            if (result == null) {
                log.error("验证关键词远程服务返回结果为空，keyword: {}", keyword);
                return Result.failMessage("验证失败，请稍后重试");
            }

            if (result.getCode() == null || result.getCode() != 200) {
                log.warn("验证关键词失败，keyword: {}, code: {}, message: {}", keyword, result.getCode(), result.getMessage());
                String errorMsg = result.getMessage() != null ? result.getMessage() : "验证失败";
                return Result.failMessage(errorMsg);
            }

            SearchVo data = result.getData();
            return Result.ok(data);

        } catch (Exception e) {
            log.error("验证关键词异常，keyword: {}", keyword, e);
            return Result.failMessage("系统繁忙，请稍后再试");
        }
    }
}

