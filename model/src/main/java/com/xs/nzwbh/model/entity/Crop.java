package com.xs.nzwbh.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xs.nzwbh.model.base.BaseEntity;
import lombok.Data;

@Data
@TableName("crop")
public class Crop extends BaseEntity {

    private String name;
    private String image;
    private String description;

}
