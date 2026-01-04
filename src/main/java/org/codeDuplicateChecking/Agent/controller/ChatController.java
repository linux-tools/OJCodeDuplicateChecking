package org.codeDuplicateChecking.Agent.controller;

import lombok.Data;
import org.codeDuplicateChecking.Agent.service.QwenService;
import org.codeDuplicateChecking.Agent.service.QwenStreamService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final QwenService qwenService;
    private final QwenStreamService qwenStreamService;

    public ChatController(QwenService qwenService, QwenStreamService qwenStreamService) {
        this.qwenService = qwenService;
        this.qwenStreamService = qwenStreamService;
    }

    /**
     * 传统对话接口
     */
    @PostMapping("/text")
    public String chat(@RequestBody ChatRequest request) throws Exception {
        return qwenService.chat(request.getMessage(), request.getSystemPrompt());
    }

    /**
     * 流式对话接口
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                // 直接传递SseEmitter给服务层，让服务层实时发送数据
                qwenStreamService.streamChat(request.getMessage(), request.getSystemPrompt(), emitter);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                executor.shutdown();
            }
        });

        return emitter;
    }

    @Data
    public static class ChatRequest {
        private String message;
        private String systemPrompt;
    }
}