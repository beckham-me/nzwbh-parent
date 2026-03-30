package com.xs.nzwbh.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xs.nzwbh.model.base.BaseEntity;
import lombok.Data;

@Data
@TableName("search_history")
public class SearchHistory extends BaseEntity {

    // 用户ID，用于关联用户
    @TableField("user_id")
    private Long userId;

    // 搜索结果关键词
    private String keyword;

    // 搜索结果类型（例如：农作物、病害等）
    @TableField("result_type")
    private String resultType;

    // 是否为热门搜索（0：否；1：是）
    @TableField("is_hot")
    private Integer isHot;

    @TableField("searchCount")
    private Long searchCount;
    // 搜索结果的来源（如：搜索引擎、推荐等）
    private String source;
}
