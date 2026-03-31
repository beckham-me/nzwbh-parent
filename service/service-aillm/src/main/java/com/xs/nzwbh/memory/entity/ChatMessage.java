package com.xs.nzwbh.memory.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("chat_message")
@Data
public class ChatMessage {

    @Id
    private String id;

    private String sessionId;
    private String role;
    private String content;
    private LocalDateTime time;
}