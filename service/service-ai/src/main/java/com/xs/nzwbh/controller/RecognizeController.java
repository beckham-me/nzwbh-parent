package com.xs.nzwbh.controller;

import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.vo.RecognizeResultVO;
import com.xs.nzwbh.model.vo.RecognizeVO;
import com.xs.nzwbh.service.RecognizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recognize")
public class RecognizeController {

    @Autowired
    private RecognizeService recognizeService;

    @PostMapping("/image")
    public Result<RecognizeResultVO> recognizeImage(@RequestPart("file") MultipartFile file, @RequestParam("userId") Long userId) {
            RecognizeResultVO result = recognizeService.recognizeImage(file,userId);
            log.info("识别结果：{}", result);
            return Result.ok(result);

    }

    @PostMapping("/video")
    public Result<RecognizeResultVO> recognizeVideo(@RequestPart("file") MultipartFile file, @RequestParam("userId") Long userId) {
        RecognizeResultVO result = recognizeService.recognizeVideo(file, userId);
        log.info("识别结果：{}", result);
        return Result.ok(result);
    }

    @GetMapping("/records")
    public Result<List<RecognizeVO>> getUserRecords(@RequestParam("userId") Long userId) {
        List<RecognizeVO> records = recognizeService.getRecordsByUserId(userId);
        return Result.ok(records);
    }

    @DeleteMapping("/records")
    public Result<?> clearUserRecords(@RequestParam("userId") Long userId) {
        recognizeService.clearRecordsByUserId(userId);
        return Result.ok("记录已清空");
    }
}
