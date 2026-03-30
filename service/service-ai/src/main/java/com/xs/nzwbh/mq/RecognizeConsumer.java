package com.xs.nzwbh.mq;

import com.xs.nzwbh.config.RabbitMQConfig;
import com.xs.nzwbh.mapper.RecognizeRecordMapper;
import com.xs.nzwbh.model.dto.RecognizeMessage;
import com.xs.nzwbh.model.entity.RecognizeRecord;
import com.xs.nzwbh.model.vo.RecognizeResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RecognizeConsumer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RecognizeRecordMapper mapper;

    @Autowired
    @Qualifier("recognizeExecutor")
    private ThreadPoolTaskExecutor executor;

    private final Semaphore semaphore = new Semaphore(3); // 最多3个任务同时跑

    @Value("${flask.url}")
    private String flaskUrl;
    // 定义线程安全的缓冲列表：暂存接收到的消息，支持并发读写
    private final List<RecognizeMessage> buffer = new CopyOnWriteArrayList<>();

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receive(RecognizeMessage msg) {
        buffer.add(msg);
    }

    // 每10秒执行
    @Scheduled(fixedDelay = 10000)
    public void batchProcess() {

        if (buffer.isEmpty()) return;

        List<RecognizeMessage> batch = new ArrayList<>(buffer);
        buffer.clear();
        // 将批量消息转换为异步任务列表
        List<CompletableFuture<Void>> futures = batch.stream()
                // 每个消息创建一个异步任务，在线程池中并行执行 process 方法
                .map(msg -> CompletableFuture.runAsync(() -> process(msg), executor))
                .collect(Collectors.toList());
        // 等待所有异步任务执行完成（allOf 聚合，join 阻塞等待）
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void process(RecognizeMessage msg) {

        try {

            semaphore.acquire(); // 获取GPU资源
            String url;

            // ✅ 区分类型
            if ("image".equals(msg.getType())) {
                url = flaskUrl + "/infer/image?file=" + msg.getFileName();
            } else {
                url = flaskUrl + "/infer/video?file=" + msg.getFileName();
            }

            RecognizeResultVO result =
                    restTemplate.getForObject(url, RecognizeResultVO.class);

            // 写Redis
            redisTemplate.opsForValue().set(
                    "recognize:" + msg.getFileHash(),  // 设置缓存键：recognize: 前缀 + 文件哈希（去重）
                    result, // 缓存值：识别结果对象
                    1, TimeUnit.DAYS  // 设置过期时间：1 天
            );

            // 写数据库
            RecognizeRecord record = new RecognizeRecord();
            record.setUserId(msg.getUserId());
            record.setFileType(msg.getType());
            record.setClassName(result.getClassName());
            record.setConfidence(result.getConfidence());
            record.setOutputUrl(result.getOutputUrl());
            record.setCreateTime(new Date());

            mapper.insert(record);

        } catch (Exception e) {
            log.error("识别失败", e);
        }finally {
            semaphore.release(); // 释放
        }
    }
}
