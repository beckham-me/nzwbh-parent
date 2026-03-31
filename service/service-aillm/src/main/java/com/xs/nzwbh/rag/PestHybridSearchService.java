package com.xs.nzwbh.rag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.xs.nzwbh.service.PestEmbeddingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PestHybridSearchService：混合检索服务（升级版🔥）
 *
 * ✔ 向量检索（dense_vector）
 * ✔ BM25关键词检索
 * ✔ ScriptScore加权融合（论文级）
 */
@Service
public class PestHybridSearchService {

    private final ElasticsearchClient client;        // ES客户端
    private final PestEmbeddingService embedding;    // 向量服务

    // 向量权重（可调）
    private static final double VECTOR_WEIGHT = 0.7;

    // BM25权重（可调）
    private static final double BM25_WEIGHT = 0.3;

    public PestHybridSearchService(ElasticsearchClient client,
                                   PestEmbeddingService embedding) {
        this.client = client;        // 注入ES
        this.embedding = embedding;  // 注入Embedding
    }

    /**
     * 混合检索入口
     */
    public String search(String query) {

        try {
            // 文本向量化
            // 获取query向量
            List<Float> vec = embedding.embed(query);


            // 构建融合评分脚本
            Script script = Script.of(b -> b
                    .inline(i -> i
                            .source(
                                    // cosine相似度 + BM25评分融合
                                    "cosineSimilarity(params.v, 'vector') * params.vw + _score * params.bw"
                            )
                            // 传入参数
                            .params("v", JsonData.of(vec))                 // 向量
                            .params("vw", JsonData.of(VECTOR_WEIGHT))      // 向量权重
                            .params("bw", JsonData.of(BM25_WEIGHT))        // BM25权重
                    )
            );

            // 构建查询（BM25部分）
            SearchResponse<PestVectorDocument> response = client.search(s -> s
                            .index("pest_vector")  // 索引名

                            .query(q -> q
                                    .scriptScore(sc -> sc
                                            .query(q2 -> q2
                                                    // 👉 BM25关键词匹配（比matchAll强很多）
                                                    .match(m -> m
                                                            .field("content")  // 匹配字段
                                                            .query(query)      // 用户输入
                                                    )
                                            )
                                            .script(script)  // 融合评分
                                    )
                            )

                            .size(5), // TopK
                    PestVectorDocument.class
            );

            // 解析结果
            List<String> results = response.hits().hits().stream()
                    .map(Hit::source)                         // 获取文档
                    .filter(doc -> doc != null)               // 防止空指针
                    .map(PestVectorDocument::getContent)      // 获取内容
                    .collect(Collectors.toList());            // 转List

            // 空结果兜底
            if (results.isEmpty()) {
                return "未检索到相关知识"; // 返回提示
            }

            // 拼接返回（RAG上下文）
            return String.join("\n", results);

        } catch (Exception e) {

            // 异常兜底（非常关键）
            e.printStackTrace();

            return "检索失败，请稍后重试";
        }
    }
}