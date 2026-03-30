package com.xs.nzwbh.aillm.client;

import com.xs.nzwbh.model.dto.ChatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value= "service-aillm")
public interface AillmFeignClient {


    @PostMapping(value = "/ai/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ChatResponse chat(@RequestPart("question") String question,
                      @RequestPart(value = "image", required = false) MultipartFile image);
}
