package com.xs.nzwbh.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xs.nzwbh.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("announcement")
public class Announcement extends BaseEntity {
    private String title;
    private String content;
}
