package com.xs.nzwbh.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.common.util.FileHashUtil;
import com.xs.nzwbh.common.util.MinioUtil;
import com.xs.nzwbh.config.RabbitMQConfig;
import com.xs.nzwbh.mapper.RecognizeRecordMapper;
import com.xs.nzwbh.model.dto.RecognizeMessage;
import com.xs.nzwbh.model.entity.RecognizeRecord;
import com.xs.nzwbh.model.vo.RecognizeResultVO;
import com.xs.nzwbh.service.RecognizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class RecognizeServiceImpl extends ServiceImpl<RecognizeRecordMapper, RecognizeRecord>
        implements RecognizeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MinioUtil minioUtil;

    private static final String CACHE_PREFIX = "recognize:";

    @Override
    public RecognizeResultVO recognizeImage(MultipartFile file, Long userId) {

        String hash = FileHashUtil.getFileMd5(file);
        String key = CACHE_PREFIX + hash;

        // 查缓存
        RecognizeResultVO cache =
                (RecognizeResultVO) redisTemplate.opsForValue().get(key);

        if (cache != null) {
            return cache;
        }

        // 上传MinIO
        String fileName = minioUtil.upload(file);

        // 发MQ
        RecognizeMessage msg = new RecognizeMessage();
        msg.setFileHash(hash);
        msg.setFileName(fileName);
        msg.setUserId(userId);
        msg.setType("image");

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                msg
        );

        // 返回处理中
        RecognizeResultVO vo = new RecognizeResultVO();
        vo.setStatus("PROCESSING");
        return vo;
    }

    @Override
    public RecognizeResultVO recognizeVideo(MultipartFile file, Long userId) {

        String hash = FileHashUtil.getFileMd5(file);
        String key = CACHE_PREFIX + hash;

        // 查缓存
        RecognizeResultVO cache =
                (RecognizeResultVO) redisTemplate.opsForValue().get(key);

        if (cache != null) {
            return cache;
        }

        // 上传 MinIO
        String fileName = minioUtil.upload(file);

        // 发MQ
        RecognizeMessage msg = new RecognizeMessage();
        msg.setFileHash(hash);
        msg.setFileName(fileName);
        msg.setUserId(userId);
        msg.setType("video");

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                msg
        );

        // 返回处理中
        RecognizeResultVO vo = new RecognizeResultVO();
        vo.setStatus("PROCESSING_VIDEO");
        return vo;
    }

    @Override
    public void clearRecordsByUserId(Long userId) {

        List<RecognizeRecord> list = this.lambdaQuery()
                .eq(RecognizeRecord::getUserId, userId)
                .list();

        // 删除Redis
        for (RecognizeRecord r : list) {
            redisTemplate.delete("recognize:" + r.getFileHash());
        }

        // 删除数据库
        this.lambdaUpdate()
                .eq(RecognizeRecord::getUserId, userId)
                .remove();
    }
}

