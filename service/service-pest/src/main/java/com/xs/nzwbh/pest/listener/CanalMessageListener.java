package com.xs.nzwbh.pest.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.xs.nzwbh.common.config.RabbitMQConfig;
import com.xs.nzwbh.model.esentity.CropDiseasesAndPestsDocument;
import com.xs.nzwbh.pest.es.repository.CropDiseasesAndPestsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CanalMessageListener {

    @Autowired
    private CropDiseasesAndPestsRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMQConfig.PEST_QUEUE)
    public void handleCanalMessage(Message message, Channel channel) throws Exception {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("收到Canal消息: {}", body);

        try {
            JsonNode root = objectMapper.readTree(body);
            String type = root.get("type").asText();      // INSERT, UPDATE, DELETE
            String table = root.get("table").asText();    // 表名
            JsonNode data = root.get("data");             // 数据数组

            if ("crop_diseases_and_pests".equals(table)) {
                for (JsonNode row : data) {
                    Long id = row.get("id").asLong();
                    if ("DELETE".equals(type)) {
                        repository.deleteById(id);
                        log.info("从ES删除文档 id={}", id);
                    } else {
                        // 转换并保存
                        CropDiseasesAndPestsDocument doc = convertToDocument(row);
                        repository.save(doc);
                        log.info("同步到ES: {} id={}", type, id);
                    }
                }
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("处理Canal消息失败", e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    private CropDiseasesAndPestsDocument convertToDocument(JsonNode row) {
        CropDiseasesAndPestsDocument doc = new CropDiseasesAndPestsDocument();
        doc.setId(row.get("id").asLong());
        doc.setCropId(row.get("crop_id").asLong());
        doc.setName(row.get("name").asText());
        doc.setType(row.get("type").asText());
        doc.setImage(row.get("image").asText());
        doc.setDescription(row.get("description").asText());
        doc.setCause(row.get("cause").asText());
        doc.setSolution(row.get("solution").asText());
        doc.setIsDeleted(row.get("is_deleted").asInt());
        // 日期字段可根据实际解析
        return doc;
    }
}
