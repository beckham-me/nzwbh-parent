package com.xs.nzwbh.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class CropDiseasesAndPestsVo {
    private String name;
    private String type; // 用于区分虫害或病害
    private Long id;
    private String image;
    private Long cropId;
    private String description;
    private String cause;
    private String solution;


}
