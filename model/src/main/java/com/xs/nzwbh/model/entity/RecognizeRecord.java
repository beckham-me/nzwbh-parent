package com.xs.nzwbh.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("recognize_record")
public class RecognizeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String filename;        // 原始文件名
    private String fileType;
    private String outputUrl;
    private String className;
    private Double confidence;
    private Date recognizeTime; // 识别时间
    private String fileHash;
    @TableField("create_time")
    private Date createTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
