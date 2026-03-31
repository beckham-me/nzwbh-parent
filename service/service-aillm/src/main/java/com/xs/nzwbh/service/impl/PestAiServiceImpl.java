package com.xs.nzwbh.service.impl;

import com.xs.nzwbh.agent.PestAgent;
import com.xs.nzwbh.memory.entity.MessageDTO;
import com.xs.nzwbh.memory.service.ChatMemoryService;
import com.xs.nzwbh.model.dto.ChatRequest;
import com.xs.nzwbh.model.dto.ChatResponse;
import com.xs.nzwbh.rag.PestHybridSearchService;
import com.xs.nzwbh.service.PestAiService;
import com.xs.nzwbh.tools.PestDetectTool;
import com.xs.nzwbh.tools.PesticideTool;
import com.xs.nzwbh.util.MinioUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public class PestAiServiceImpl implements PestAiService {

    private final ChatMemoryService memory;      // 会话记忆
    private final PestHybridSearchService rag;   // RAG
    private final PestAgent agent;               // AI推理
    private final MinioUtil minioUtil;           // MinIO

    private final PestDetectTool detectTool;     // YOLO
    private final PesticideTool pesticideTool;   // 农药推荐

    public PestAiServiceImpl(ChatMemoryService memory,
                             PestHybridSearchService rag,
                             PestAgent agent,
                             MinioUtil minioUtil,
                             PestDetectTool detectTool,
                             PesticideTool pesticideTool) {
        this.memory = memory;
        this.rag = rag;
        this.agent = agent;
        this.minioUtil = minioUtil;
        this.detectTool = detectTool;
        this.pesticideTool = pesticideTool;
    }

    @Override
    public ChatResponse chat(ChatRequest req, MultipartFile image) {

        String sid = req.getSessionId();
        String question = req.getQuestion();

        String imageUrl = null;
        String pestName = null;

        // 图片处理（MinIO + YOLO）
        if (image != null && !image.isEmpty()) {

            // 校验类型
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("只允许图片");
            }

            // 上传MinIO
            imageUrl = minioUtil.upload(image);

            // YOLO识别（只调用一次）
            pestName = detectTool.detect(imageUrl);
        }

        // 无图片 → 用文本
        if (pestName == null || pestName.isBlank()) {
            pestName = question;
        }

        // 农药推荐（RAG）
        String pesticideInfo = pesticideTool.recommend(pestName);

        //  RAG知识
        String knowledge;
        try {
            knowledge = rag.search(pestName);
        } catch (Exception e) {
            knowledge = "";
        }

        // 保存用户原始问题（不要污染）
        memory.save(sid, "user", question);

        // 获取结构化上下文
        List<MessageDTO> context = memory.context(sid);

        // 调用Agent
        String answer = agent.run(
                context,
                knowledge,
                question,
                pestName,
                pesticideInfo
        );

        // 保存AI回复
        memory.save(sid, "assistant", answer);

        return new ChatResponse(answer);
    }
}