package com.xs.nzwbh.model.vo;

import lombok.Data;
import java.util.Date;
@Data
public class RecognizeVO {
    private String fileType;        // 文件类型（image / video）
    private String className;       // 类别名称
    private Double confidence;      // 置信度
    private Date recognizeTime;     // 识别时间
    private String outputUrl;       // 输出文件地址（图片或视频）
}
