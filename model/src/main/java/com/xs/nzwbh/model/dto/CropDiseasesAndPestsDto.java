package com.xs.nzwbh.model.dto;


import lombok.Data;

@Data
public class CropDiseasesAndPestsDto {
    private Long id;
    private String name;
    private String type; // 用于区分虫害或病害
    private String image;
    private Long cropId;
    private String description;
    private String cause;
    private String solution;
}
