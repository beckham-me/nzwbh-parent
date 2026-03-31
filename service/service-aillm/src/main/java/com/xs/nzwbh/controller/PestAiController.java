package com.xs.nzwbh.controller;

import com.xs.nzwbh.model.dto.ChatRequest;
import com.xs.nzwbh.model.dto.ChatResponse;
import com.xs.nzwbh.service.PestAiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai")
public class PestAiController {

    private final PestAiService service;

    public PestAiController(PestAiService service) {
        this.service = service;
    }

    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatResponse chat(
            @ModelAttribute ChatRequest req,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        return service.chat(req, image);
    }
}
