package com.xs.nzwbh.model.mgrvo;

import lombok.Data;

import java.util.List;

@Data
public class Crop1Vo {
    private Long id;
    private String name;
    private List<String> disease;
    private String image;
    private String description;
}
