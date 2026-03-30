package com.xs.nzwbh.model.dto;

import lombok.Data;

@Data
public class RecognizeMessage {
    private String fileHash;
    private String fileName;
    private Long userId;
    private String type;
}
