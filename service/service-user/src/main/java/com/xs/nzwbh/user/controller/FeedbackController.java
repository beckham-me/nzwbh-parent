package com.xs.nzwbh.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.PageResult;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.common.util.AuthContextHolder;
import com.xs.nzwbh.model.dto.FeedbackDTO;
import com.xs.nzwbh.model.entity.Feedback;
import com.xs.nzwbh.model.vo.FeedbackVO;
import com.xs.nzwbh.user.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/save")
    public Result<?> saveFeedback(@RequestParam("userId") Long userId,@RequestBody FeedbackDTO feedback) {



        // 将问题和描述与 userId 一起保存
        Feedback entity = new Feedback();
        entity.setUserId(userId);
        entity.setProblem(feedback.getProblem());
        entity.setDescription(feedback.getDescription());
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setIsDeleted(0);
        feedbackService.save(entity);
        return Result.ok("反馈已提交");
    }



    @GetMapping("/page")
    public Result<PageResult<List<FeedbackVO>>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(feedbackService.getFeedbackPage(page, limit));
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = feedbackService.softDeleteById(id);
        return success ? Result.ok() : Result.failMessage("删除失败或数据不存在");
    }

}
