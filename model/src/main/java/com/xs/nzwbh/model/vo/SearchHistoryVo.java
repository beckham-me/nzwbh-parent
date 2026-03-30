package com.xs.nzwbh.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xs.nzwbh.model.base.BaseEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("search_history")
public class SearchHistoryVo extends BaseEntity {

    // 用户ID，用于关联用户
    private Integer userId;

    // 搜索结果关键词
    private String keyword;

    // 搜索结果类型（例如：农作物、病害等）
    private String resultType;

    // 是否为热门搜索（0：否；1：是）
    private Integer isHot;

    // 搜索结果的来源（如：搜索引擎、推荐等）
    private String source;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;
}
