package com.xs.nzwbh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.common.result.PageResult;
import com.xs.nzwbh.model.entity.Feedback;
import com.xs.nzwbh.model.vo.FeedbackVO;
import com.xs.nzwbh.user.mapper.FeedbackMapper;
import com.xs.nzwbh.user.service.FeedbackService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户反馈服务实现类
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    /**
     * 分页查询反馈列表
     *
     * @param page  当前页码
     * @param limit 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<List<FeedbackVO>> getFeedbackPage(int page, int limit) {
        // 参数校验与默认值处理
        int currentPage = page <= 0 ? 1 : page;
        int pageSize = limit <= 0 ? 10 : limit;

        // 创建分页对象
        Page<Feedback> feedbackPage = new Page<>(currentPage, pageSize);

        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getIsDeleted, 0)
                .orderByDesc(Feedback::getCreateTime);

        // 执行分页查询
        IPage<Feedback> rawPage = this.page(feedbackPage, wrapper);

        // 转换为 VO 列表
        List<FeedbackVO> voList = rawPage.getRecords().stream()
                .map(feedback -> {
                    FeedbackVO vo = new FeedbackVO();
                    BeanUtils.copyProperties(feedback, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        // 封装分页结果
        PageResult<List<FeedbackVO>> result = new PageResult<>();
        result.setTotal(rawPage.getTotal());
        result.setPages((int) rawPage.getPages());
        result.setCurrent((int) rawPage.getCurrent());
        result.setSize((int) rawPage.getSize());
        result.setRecords(voList);

        return result;
    }

    /**
     * 根据ID软删除反馈
     *
     * @param id 反馈ID
     * @return 是否删除成功
     */
    @Override
    public boolean softDeleteById(Long id) {
        // 此处为物理删除，若需软删除可改为更新 is_deleted 字段
        return this.removeById(id);
    }
}