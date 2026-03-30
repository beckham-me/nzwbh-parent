package com.xs.nzwbh.model.dto;

import lombok.Data;

@Data
public class ToolResult {

    private String pest;       // 虫害名称，例如 “蚜虫”
    private double confidence; // 置信度（0~1）
    private String raw;        // 原始返回信息（可选，用于调试）
}