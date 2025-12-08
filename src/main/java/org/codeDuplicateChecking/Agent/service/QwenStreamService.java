package org.codeDuplicateChecking.Agent.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.codeDuplicateChecking.Agent.config.QwenConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class QwenStreamService {
    private final Generation generation;
    private final QwenConfig.QwenProperties qwenProperties;

    public QwenStreamService(Generation generation, QwenConfig.QwenProperties qwenProperties) {
        this.generation = generation;
        this.qwenProperties = qwenProperties;
    }

    public void streamChat(String userMessage, String systemPrompt, SseEmitter emitter)
            throws ApiException, NoApiKeyException, InputRequiredException, IOException {

        List<Message> messages = new ArrayList<>();

        // 添加系统提示
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build());
        }

        // 添加用户消息
        messages.add(Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage)
                .build());

        // 构建请求参数（启用流式输出）
        GenerationParam param = GenerationParam.builder()
                .apiKey(qwenProperties.getApiKey())
                .model(qwenProperties.getModel())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true)
                .build();

        // 处理流式响应，每次接收数据时立即通过SseEmitter发送
        generation.streamCall(param).blockingForEach(result -> {
            try {
                if (result.getOutput() != null && !result.getOutput().getChoices().isEmpty()) {
                    // 提取当前增量输出内容
                    String content = result.getOutput().getChoices().get(0).getMessage().getContent();
                    // 通过SseEmitter发送数据
                    emitter.send(SseEmitter.event().data(content != null ? content : ""));
                }
            } catch (Exception e) {
                // 发生异常时，中断流式输出
                emitter.completeWithError(e);
                throw new RuntimeException("Error sending stream data", e);
            }
        });
    }
}
