package com.xs.nzwbh.model.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;
    private String question;
    private String imageUrl;
}
