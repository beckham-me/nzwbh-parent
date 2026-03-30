package com.xs.nzwbh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xs.nzwbh.common.result.PageResult;
import com.xs.nzwbh.model.entity.Feedback;
import com.xs.nzwbh.model.vo.FeedbackVO;

import java.util.List;

public interface FeedbackService extends IService<Feedback> {

    PageResult<List<FeedbackVO>> getFeedbackPage(int page, int limit);
    boolean softDeleteById(Long id);
}
