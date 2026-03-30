package com.xs.nzwbh.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xs.nzwbh.model.base.BaseEntity;
import lombok.Data;

@Data
@TableName("feedback")
public class Feedback extends BaseEntity {

    private Long userId;
    private String problem;
    private String description;

}
