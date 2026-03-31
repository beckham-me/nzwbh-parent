package com.xs.nzwbh.memory.service;

import com.xs.nzwbh.memory.entity.ChatMessage;
import com.xs.nzwbh.memory.entity.MessageDTO;
import com.xs.nzwbh.memory.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMemoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatMemoryService.class);

    private final ChatMessageRepository repo; // Mongo
    private final RedisTemplate<String, Object> redis; // Redis
    private final ChatClient chatClient; // 用于生成摘要


    private static final int WINDOW = 10;              // 短期窗口
    private static final int MAX_CHARS = 4000;         // token限制
    private static final Duration TTL = Duration.ofHours(12); // Redis过期

    public ChatMemoryService(ChatMessageRepository repo,
                             RedisTemplate<String, Object> redis,
                             ChatClient chatClient) {
        this.repo = repo;
        this.redis = redis;
        this.chatClient = chatClient;
    }

    /**
     * 保存对话（Mongo + Redis）
     */
    public void save(String sid, String role, String content) {

        // 去噪（过滤无意义内容）,太短直接丢弃
        if (content == null || content.trim().length() < 2) {
            return;
        }

        // 保存到Mongo
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sid); // 会话ID
        msg.setRole(role);     // 角色
        msg.setContent(content); // 内容
        msg.setTime(LocalDateTime.now()); // 时间

        repo.save(msg);

        // Redis Key（多会话隔离）
        String key = buildKey(sid);

        // 转结构化对象
        MessageDTO dto = new MessageDTO(role, content);

        // 写入Redis
        redis.opsForList().rightPush(key, dto);

        // 裁剪窗口（防止无限增长）
        redis.opsForList().trim(key, -WINDOW, -1);

        // 设置TTL（避免垃圾数据）
        redis.expire(key, TTL);
    }

    /**
     * 获取上下文（结构化）
     */
    public List<MessageDTO> context(String sid) {

        String key = buildKey(sid);

        // 先查Redis
        List<Object> cached = redis.opsForList().range(key, 0, -1);

        List<MessageDTO> messages;

        if (cached != null && !cached.isEmpty()) {

            // 命中缓存
            messages = cached.stream()
                    .map(o -> (MessageDTO) o) // 类型转换
                    .collect(Collectors.toList());

        } else {

            // Redis未命中 → 查Mongo
            List<ChatMessage> list = repo.findBySessionIdOrderByTimeAsc(sid);

            messages = list.stream()
                    .map(m -> new MessageDTO(m.getRole(), m.getContent()))
                    .collect(Collectors.toList());

            // 回填Redis（性能优化）
            for (MessageDTO dto : messages) {
                redis.opsForList().rightPush(key, dto);
            }

            redis.opsForList().trim(key, -WINDOW, -1);
            redis.expire(key, TTL);
        }

        // 摘要记忆
        String summary = getSummary(sid);

        List<MessageDTO> finalContext = new ArrayList<>();

        if (summary != null) {
            finalContext.add(
                    new MessageDTO("system", "【历史摘要】" + summary)
            );
        }

        finalContext.addAll(messages);

        // Token控制（字符裁剪）
        return trimByLength(finalContext);
    }

    /**
     * 生成摘要（长对话记忆）
     */
    private String getSummary(String sid) {

        String key = "chat:summary:" + sid;

        // 先查Redis
        Object cached = redis.opsForValue().get(key);

        if (cached != null) {
            return cached.toString();
        }

        // 查Mongo
        List<ChatMessage> list = repo.findBySessionIdOrderByTimeAsc(sid);
        // 对话太短不摘要
        if (list.size() < 20) {
            return null;
        }

        // 拼接历史
        String history = list.stream()
                .map(m -> m.getRole() + ":" + m.getContent())
                .collect(Collectors.joining("\n"));

        // 调用LLM生成摘要
        String prompt = """
        请总结以下对话，提取：
        1. 作物
        2. 虫害
        3. 用户问题

        对话：
        %s
        """.formatted(history);

        String summary = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 缓存摘要
        redis.opsForValue().set(key, summary, TTL);

        return summary;
    }

    /**
     * 上下文裁剪（模拟token限制）
     */
    private List<MessageDTO> trimByLength(List<MessageDTO> list) {

        int total = 0;
        List<MessageDTO> result = new ArrayList<>();

        // 从后往前（保留最新）
        for (int i = list.size() - 1; i >= 0; i--) {

            MessageDTO msg = list.get(i);

            int len = msg.getContent().length();

            if (total + len > MAX_CHARS) {
                break;
            }

            result.add(0, msg); // 头插
            total += len;
        }

        return result;
    }

    /**
     * Redis key构造（会话隔离）
     */
    private String buildKey(String sid) {
        return "chat:session:" + sid;
    }
}
