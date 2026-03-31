package com.xs.nzwbh.rag;

import lombok.Data;
import java.util.List;

/**
 * ES向量文档
 */
@Data
public class PestVectorDocument {

    private String id;          // 文档ID

    private String content;     // 文本内容

    private List<Float> vector; // 向量

    private String fileName;    // 来源文件

    private Integer page;       // 页码

    private Integer chunkIndex; // 分块序号
}