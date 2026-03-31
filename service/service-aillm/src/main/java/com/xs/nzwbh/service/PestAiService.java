package com.xs.nzwbh.service;

import com.xs.nzwbh.model.dto.ChatRequest;
import com.xs.nzwbh.model.dto.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PestAiService {

    ChatResponse chat(ChatRequest req, MultipartFile image);
}
