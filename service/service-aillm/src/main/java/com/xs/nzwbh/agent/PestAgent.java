package com.xs.nzwbh.agent;


import com.xs.nzwbh.memory.entity.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PestAgent {

    private static final Logger log = LoggerFactory.getLogger(PestAgent.class);

    private final ChatClient chatClient;

    public PestAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Agent推理
     */
    public String run(List<MessageDTO> context,
                      String knowledge,
                      String question,
                      String pestName,
                      String pesticideInfo) {


        // 构建 messages
        List<Message> messages = new ArrayList<>();

        //system（最关键）
        messages.add(new SystemMessage("""
你是农业病虫害专家，请基于以下信息回答：

【虫害识别结果】
%s

【农药推荐】
%s

【知识库】
%s

【要求】
1. 不得编造虫害
2. 优先使用识别结果
3. 农药需说明作用机制
4. 如果不确定，请说明
"""
                .formatted(pestName, pesticideInfo, knowledge)));

        // 历史上下文
        for (MessageDTO dto : context) {

            switch (dto.getRole()) {

                case "user":
                    messages.add(new UserMessage(dto.getContent()));
                    break;

                case "assistant":
                    messages.add(new AssistantMessage(dto.getContent()));
                    break;

                default:
                    messages.add(new SystemMessage(dto.getContent()));
            }
        }

        // 当前问题
        messages.add(new UserMessage(question));

        // 调用LLM
        String result = chatClient.prompt()
                .messages(messages)
                .call()
                .content();

        log.info("AI回答: {}", result);

        return result;
    }
}
