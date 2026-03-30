package com.xs.nzwbh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xs.nzwbh.model.entity.RecognizeRecord;
import com.xs.nzwbh.model.vo.RecognizeResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface RecognizeService extends IService<RecognizeRecord> {

    RecognizeResultVO recognizeImage(MultipartFile file, Long userId);
    RecognizeResultVO recognizeVideo(MultipartFile file, Long userId);

    void clearRecordsByUserId(Long userId);
}