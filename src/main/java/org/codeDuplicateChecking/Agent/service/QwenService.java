package org.codeDuplicateChecking.Agent.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.codeDuplicateChecking.Agent.config.QwenConfig;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class QwenService {
    private final Generation generation;
    private final QwenConfig.QwenProperties qwenProperties;

    public QwenService(Generation generation, QwenConfig.QwenProperties qwenProperties) {
        this.generation = generation;
        this.qwenProperties = qwenProperties;
    }

    public String chat(String userMessage, String systemPrompt) throws ApiException, NoApiKeyException, InputRequiredException {
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

        // 构建请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(qwenProperties.getApiKey())
                .model(qwenProperties.getModel())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        // 调用API
        GenerationResult result = generation.call(param);

        // 提取AI回复
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }
}
