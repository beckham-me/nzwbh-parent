package com.xs.nzwbh.model.dto;

import lombok.Data;

@Data
public class ToolDecision {

    private String tool;       // 工具名称，例如 pest_detect
    private String input;      // 工具输入，例如 imageUrl
}
