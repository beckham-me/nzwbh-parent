package com.xs.nzwbh.service;

import org.springframework.ai.embedding.EmbeddingModel; // Spring AI embedding接口
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * PestEmbeddingService：向量生成服务
 */
@Service
public class PestEmbeddingService {

    private final EmbeddingModel embeddingModel; // 向量模型

    public PestEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 文本转向量
     */
    public List<Float> embed(String text) {

        // 调用模型生成向量
        float[] embeddingArray = embeddingModel.embed(text);

        // 转List（ES要求）
        List<Float> result = new ArrayList<>(embeddingArray.length);

        // 遍历数组
        for (float v : embeddingArray) {
            result.add(v); // 加入List
        }

        return result; // 返回向量
    }
}