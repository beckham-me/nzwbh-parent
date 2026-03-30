package com.xs.nzwbh.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xs.nzwbh.model.base.BaseEntity;
import lombok.Data;

@Data
@TableName("pest")
public class CropDiseasesAndPests extends BaseEntity {
    private String name;
    private String type; // 用于区分虫害或病害
    private String image;
    @TableField("crop_id")
    private Long cropId;
    private String description;
    private String cause;
    private String solution;

    // 调试输出
    public void debug() {
        System.out.println("Name: " + name + ", Image: " + image);
    }
}
