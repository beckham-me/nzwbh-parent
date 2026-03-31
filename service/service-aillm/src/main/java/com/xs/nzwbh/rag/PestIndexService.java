package com.xs.nzwbh.rag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.xs.nzwbh.model.dto.TextChunk;
import com.xs.nzwbh.service.PestEmbeddingService;
import com.xs.nzwbh.util.TextChunkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PestIndexService：RAG入库服务
 */
@Service
public class PestIndexService {

    private static final Logger log = LoggerFactory.getLogger(PestIndexService.class);

    private static final String INDEX = "pest_vector"; // ES索引名

    private final ElasticsearchClient client;
    private final PestEmbeddingService embeddingService;

    public PestIndexService(ElasticsearchClient client,
                            PestEmbeddingService embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
    }

    /**
     * 批量入库PDF
     */
    public void indexAllPdf(String folderPath) throws IOException {

        File folder = new File(folderPath);

        // 校验目录
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("目录不存在：" + folderPath);
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".pdf"));

        if (files == null || files.length == 0) {
            log.warn("目录下没有PDF文件");
            return;
        }

        //遍历文件
        for (File file : files) {

            String fileName = file.getName();

            log.info("📄 开始处理文件：{}", fileName);

            try {

                // 文件级去重
                long count = client.count(c -> c
                        .index(INDEX)
                        .query(q -> q
                                .term(t -> t
                                        .field("fileName.keyword") // keyword字段
                                        .value(fileName)
                                )
                        )
                ).count();

                if (count > 0) {
                    log.warn("已入库，跳过：{}", fileName);
                    continue;
                }

                // 解析
                List<TextChunk> chunks = TextChunkUtil.parsePdf(file);

                log.info("分块数量：{}", chunks.size());

                // 批量构建请求
                List<BulkOperation> operations = new ArrayList<>();

                for (TextChunk chunk : chunks) {

                    String id = UUID.randomUUID().toString();

                    //向量化
                    List<Float> vector = embeddingService.embed(chunk.getContent());

                    //构建文档
                    PestVectorDocument doc = new PestVectorDocument();
                    doc.setId(id);
                    doc.setContent(chunk.getContent());
                    doc.setVector(vector);
                    doc.setFileName(fileName);

                    // metadata
                    doc.setPage(chunk.getPage());
                    doc.setChunkIndex(chunk.getIndex());

                    // 加入bulk
                    operations.add(BulkOperation.of(op -> op
                            .index(idx -> idx
                                    .index(INDEX)
                                    .id(id)
                                    .document(doc)
                            )
                    ));

                    // 分批提交（防止过大）
                    if (operations.size() >= 100) {

                        client.bulk(b -> b.operations(operations));

                        operations.clear();

                        log.info("已提交100条");
                    }
                }

                // 提交剩余
                if (!operations.isEmpty()) {
                    client.bulk(b -> b.operations(operations));
                }

                log.info("入库完成：{}", fileName);

            } catch (Exception e) {

                log.error("文件处理失败：{}", fileName, e);
            }
        }
    }
}