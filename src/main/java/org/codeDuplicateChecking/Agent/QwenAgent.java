package org.codeDuplicateChecking.Agent;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import java.util.ArrayList;
import java.util.List;

public class QwenAgent {
    private String apiKey;
    private List<Message> conversationHistory;
    private Generation generation;

    public QwenAgent(String apiKey, String systemPrompt) {
        this.apiKey = apiKey;
        this.conversationHistory = new ArrayList<>();
        this.generation = new Generation();
        Message AIMessage = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(systemPrompt)
                .build();
        this.conversationHistory.add(AIMessage);
    }

    public String chat(String userMessage) throws ApiException, NoApiKeyException, InputRequiredException {
        // 添加用户消息到对话历史
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage)
                .build();
        conversationHistory.add(userMsg);

        // 构建API请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(this.apiKey)
                .model("qwen-plus") // 通义千问Plus模型，平衡性能和成本
                .messages(this.conversationHistory)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();


        // 调用API
        GenerationResult result = this.generation.call(param);

        // 获取AI的回复
        String aiResponse = result.getOutput().getChoices().get(0).getMessage().getContent();

        // 添加AI回复到对话历史
        Message aiMsg = Message.builder()
                .role(Role.ASSISTANT.getValue())
                .content(aiResponse)
                .build();
        conversationHistory.add(aiMsg);
        return aiResponse;
    }

}
