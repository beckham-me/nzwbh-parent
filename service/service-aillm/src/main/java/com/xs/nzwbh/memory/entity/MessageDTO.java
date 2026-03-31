package com.xs.nzwbh.memory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private String role;     // user / assistant / system
    private String content;  // 内容
}