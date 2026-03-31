package com.xs.nzwbh.memory.repository;

import com.xs.nzwbh.memory.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findBySessionIdOrderByTimeAsc(String sessionId);
}
