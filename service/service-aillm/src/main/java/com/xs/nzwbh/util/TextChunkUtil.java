package com.xs.nzwbh.util;

import com.xs.nzwbh.model.dto.TextChunk;
import org.apache.pdfbox.pdmodel.PDDocument;           // PDF文档
import org.apache.pdfbox.text.PDFTextStripper;        // 文本提取



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本处理工具（生产级）
 *
 * ✔ 分页解析（避免OOM）
 * ✔ 段落优先
 * ✔ 句子级分块
 * ✔ 动态chunk（按句子数）
 * ✔ 保留metadata（page + index）
 */
public class TextChunkUtil {

    // ================= 参数配置 =================

    private static final int MAX_SENTENCES = 5;   // 每块最大句子数（动态chunk）
    private static final int MIN_SENTENCES = 2;   // 最小句子数（避免过碎）

    /**
     * 主入口：PDF → TextChunk列表
     */
    public static List<TextChunk> parsePdf(File file) throws IOException {

        List<TextChunk> chunks = new ArrayList<>();

        // 加载PDF（自动关闭）
        try (PDDocument document = PDDocument.load(file)) {

            PDFTextStripper stripper = new PDFTextStripper();

            stripper.setSortByPosition(true); // 保持文本顺序

            int totalPages = document.getNumberOfPages(); // 总页数

            int globalIndex = 0; // 全局块索引

            // 分页解析（内存优化）
            for (int page = 1; page <= totalPages; page++) {

                stripper.setStartPage(page); // 当前页
                stripper.setEndPage(page);

                // 提取当前页文本
                String text = stripper.getText(document);

                // 文本清洗
                text = cleanText(text);

                // 段落切分
                List<String> paragraphs = splitParagraphs(text);

                // 段落 → 句子 → chunk
                for (String paragraph : paragraphs) {

                    List<String> sentences = splitSentences(paragraph);

                    List<String> sentenceChunks = buildChunks(sentences);

                    // 转为TextChunk
                    for (String content : sentenceChunks) {

                        TextChunk chunk = new TextChunk(
                                content,       // 内容
                                page,          // 页码
                                globalIndex++  // 块序号
                        );

                        chunks.add(chunk);
                    }
                }
            }
        }

        return chunks;
    }

    /**
     * 文本清洗
     */
    private static String cleanText(String text) {

        if (text == null) return "";

        return text
                .replaceAll("\\r", "")            // 去回车
                .replaceAll("\\n{2,}", "\n\n")    // 多换行归一
                .replaceAll("[ \\t]+", " ")       // 多空格压缩
                .trim();
    }

    /**
     * 段落切分
     */
    private static List<String> splitParagraphs(String text) {

        List<String> result = new ArrayList<>();

        // 按空行切分
        String[] arr = text.split("\\n\\n");

        for (String p : arr) {

            String trimmed = p.trim();

            if (trimmed.length() > 20) { // 过滤太短段落
                result.add(trimmed);
            }
        }

        return result;
    }

    /**
     * 句子切分（中英文兼容）
     */
    private static List<String> splitSentences(String paragraph) {

        List<String> result = new ArrayList<>();

        // 正则：句号、问号、感叹号
        String[] arr = paragraph.split("(?<=[。！？.!?])");

        for (String s : arr) {

            String trimmed = s.trim();

            if (trimmed.length() > 5) { // 去掉短句
                result.add(trimmed);
            }
        }

        return result;
    }

    /**
     * 构建chunk（动态：按句子数）
     */
    private static List<String> buildChunks(List<String> sentences) {

        List<String> chunks = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        int count = 0; // 当前句子数

        for (String sentence : sentences) {

            current.append(sentence).append(" "); // 拼接句子
            count++;

            // 达到最大句子数 → 切块
            if (count >= MAX_SENTENCES) {

                chunks.add(current.toString().trim());

                current.setLength(0); // 清空
                count = 0;
            }
        }

        // 处理剩余
        if (current.length() > 0) {

            // 如果太短，合并到上一块
            if (!chunks.isEmpty() && count < MIN_SENTENCES) {

                int lastIndex = chunks.size() - 1;

                chunks.set(lastIndex,
                        chunks.get(lastIndex) + " " + current.toString().trim());

            } else {

                chunks.add(current.toString().trim());
            }
        }

        return chunks;
    }
}