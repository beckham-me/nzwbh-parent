package com.xs.nzwbh.crop.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.xs.nzwbh.common.config.RabbitMQConfig;
import com.xs.nzwbh.crop.es.CropRepository;
import com.xs.nzwbh.model.esentity.CropDocument;
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
    private CropRepository cropRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMQConfig.CROP_QUEUE)
    public void handleCanalMessage(Message message, Channel channel) throws Exception {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("收到Canal消息: {}", body);

        try {
            JsonNode root = objectMapper.readTree(body);
            String type = root.get("type").asText();
            String table = root.get("table").asText();
            JsonNode data = root.get("data");

            if ("crop".equals(table)) {
                for (JsonNode row : data) {
                    Long id = row.get("id").asLong();
                    if ("DELETE".equals(type)) {
                        cropRepository.deleteById(id);
                        log.info("从ES删除作物 id={}", id);
                    } else {
                        CropDocument doc = convertToDocument(row);
                        cropRepository.save(doc);
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

    private CropDocument convertToDocument(JsonNode row) {
        CropDocument doc = new CropDocument();
        doc.setId(row.get("id").asLong());
        doc.setName(row.get("name").asText());
        doc.setDescription(row.get("description").asText());
        doc.setImage(row.get("image").asText());
        doc.setIsDeleted(row.get("is_deleted").asInt());
        return doc;
    }
}