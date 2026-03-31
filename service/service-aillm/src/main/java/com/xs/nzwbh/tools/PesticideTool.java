package com.xs.nzwbh.tools;

import com.xs.nzwbh.rag.PestHybridSearchService;
import org.springframework.stereotype.Component;

/**
 * PesticideTool：农药推荐工具（纯RAG，不再做识别）
 */
@Component
public class PesticideTool {

    // 注入混合检索服务（RAG + 向量检索）
    private final PestHybridSearchService hybridSearchService;

    public PesticideTool(PestHybridSearchService hybridSearchService) {
        this.hybridSearchService = hybridSearchService;
    }

    /**
     * 根据虫害名称推荐农药（唯一入口）
     *
     * @param pestName 虫害名称（必须是已经确定的）
     * @return 推荐方案
     */
    public String recommend(String pestName) {

        // 判空处理
        if (pestName == null || pestName.trim().isEmpty()) {
            return "未提供虫害信息，无法推荐农药";
        }

        try {
            //调用RAG检索（核心逻辑）
            String knowledge = hybridSearchService.search(pestName);

            // 命中返回
            if (knowledge != null && !knowledge.isEmpty()) {
                return knowledge;
            }

        } catch (Exception e) {
            // 防止异常影响主流程
            e.printStackTrace();
        }

        return "未找到对应农药方案，建议咨询农业专家";
    }
}