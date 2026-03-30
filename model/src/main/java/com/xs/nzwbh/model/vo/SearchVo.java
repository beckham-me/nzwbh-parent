package com.xs.nzwbh.model.vo;

import lombok.Data;

@Data
public class SearchVo {
    private Long id;
    private String name;
    private String type; // 用于区分虫害或病害
}
