package com.xs.nzwbh.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xs.nzwbh.model.base.BaseEntity;
import lombok.Data;

@Data
@TableName("pest_detections")
public class PestDetection extends BaseEntity {
    private Double latitude;
    private Double longitude;

    /**
     * 虫害等级: 0=无虫害, 1=少量, 2=严重
     */
    private Integer level;
}
