package com.xs.nzwbh.model.mgrvo;

import lombok.Data;

@Data
public class DiseasesAndPestsVo {
    private Long id;
    private String name;
    private String type; // 用于区分虫害或病害
    private String image;
    private String description;
    private String cause;
    private String solution;
}
