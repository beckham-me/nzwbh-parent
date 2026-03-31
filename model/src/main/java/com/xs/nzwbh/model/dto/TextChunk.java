package com.xs.nzwbh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextChunk {

    private String content; // 分块文本内容

    private int page;       // 来源页码（PDF）

    private int index;      // 全局块序号（用于排序/回溯）
}