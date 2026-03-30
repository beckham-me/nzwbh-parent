package com.xs.nzwbh.model.vo;

import lombok.Data;

@Data
public class RecognizeResultVO {
    private String outputUrl;
    private String className;
    private Double confidence;

    public void setStatus(String processing) {
    }
}
