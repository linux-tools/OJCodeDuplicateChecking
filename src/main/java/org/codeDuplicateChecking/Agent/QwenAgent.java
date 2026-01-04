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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QwenAgent {
    private String apiKey;
    private String model;
    private List<Message> conversationHistory;
    private Generation generation;

    public QwenAgent(String apiKey, String model, String systemPrompt) {
        this.apiKey = apiKey;
        this.model = model;
        this.conversationHistory = new ArrayList<>();
        this.generation = new Generation();
        Message AIMessage = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(systemPrompt)
                .build();
        this.conversationHistory.add(AIMessage);
    }

    public String chat(String userMessage) throws ApiException, NoApiKeyException, InputRequiredException, TimeoutException {
        // 添加用户消息到对话历史
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage)
                .build();
        conversationHistory.add(userMsg);

        // 构建API请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(this.apiKey)
                .model(this.model) // 使用配置的模型
                .messages(this.conversationHistory)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        // 使用ExecutorService实现超时控制
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<GenerationResult> future = executor.submit(() -> {
            try {
                return generation.call(param);
            } catch (Exception e) {
                if (e instanceof RuntimeException && e.getCause() != null) {
                    // 解包运行时异常
                    Throwable cause = e.getCause();
                    if (cause instanceof ApiException) {
                        throw (ApiException) cause;
                    } else if (cause instanceof InputRequiredException) {
                        throw (InputRequiredException) cause;
                    } else if (cause instanceof NoApiKeyException) {
                        throw (NoApiKeyException) cause;
                    }
                }
                throw e;
            }
        });

        GenerationResult result;
        try {
            // 设置10秒超时
            result = future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("请求被中断", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ApiException) {
                throw (ApiException) cause;
            } else if (cause instanceof InputRequiredException) {
                throw (InputRequiredException) cause;
            } else if (cause instanceof NoApiKeyException) {
                throw (NoApiKeyException) cause;
            } else {
                throw new RuntimeException("请求执行失败", e);
            }
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("AI服务连接超时");
        } finally {
            executor.shutdown();
        }

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
    
    /**
     * 检查AI服务连接是否正常
     * @return true如果连接正常，false否则
     * @throws TimeoutException 如果连接超时
     */
    public boolean checkConnection() throws TimeoutException {
        try {
            // 发送一个简单的测试消息来检查连接
            String testResponse = chat("请返回'OK'以确认连接正常");
            return testResponse != null && testResponse.contains("OK");
        } catch (TimeoutException e) {
            throw e; // 重新抛出TimeoutException以便Controller处理
        } catch (Exception e) {
            return false;
        }
    }

}