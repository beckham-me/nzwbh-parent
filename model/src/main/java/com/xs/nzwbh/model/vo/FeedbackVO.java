package com.xs.nzwbh.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class FeedbackVO {
    private Long id;
    private Long userId;
    private String problem;
    private String description;
    private Date createTime;
}
