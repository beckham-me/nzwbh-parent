package com.xs.nzwbh.ai.client;

import com.xs.nzwbh.ai.config.FeignConfig;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.vo.RecognizeResultVO;
import com.xs.nzwbh.model.vo.RecognizeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(value = "service-ai", configuration = FeignConfig.class)
public interface AiFeignClient {

    @PostMapping(value = "/recognize/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<RecognizeResultVO> recognizeImage(@RequestPart("file") MultipartFile file, @RequestParam("userId") Long userId);

    @PostMapping(value = "/recognize/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<RecognizeResultVO> recognizeVideo(@RequestPart("file") MultipartFile file, @RequestParam("userId") Long userId);

    @GetMapping("/recognize/records")
    public Result<List<RecognizeVO>> getUserRecords(@RequestParam("userId") Long userId);

    @DeleteMapping("/records")
    public Result<?> clearUserRecords(@RequestParam("userId") Long userId);
}
