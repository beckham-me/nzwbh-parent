package com.xs.nzwbh.init;

import com.xs.nzwbh.rag.PestIndexService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * RAG初始化（带去重）
 */
@Component
public class RagDataInitializer {

    private final PestIndexService pestIndexService;

    public RagDataInitializer(PestIndexService pestIndexService) {
        this.pestIndexService = pestIndexService;
    }

    @PostConstruct
    public void init() {

        try {
            // 获取项目根目录 + text目录
            String path = System.getProperty("user.dir") + "/text";

            // 执行入库（自动跳过已存在文件）
            pestIndexService.indexAllPdf(path);

            System.out.println(" RAG初始化完成");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}