package org.codeDuplicateChecking.Agent.controller;

import org.codeDuplicateChecking.Agent.QwenAgent;
import org.codeDuplicateChecking.Agent.config.AIPromptConfig;
import org.codeDuplicateChecking.Agent.model.ImprovementRequest;
import org.codeDuplicateChecking.Agent.model.SinglePlagiarismRequest;
import org.codeDuplicateChecking.Agent.model.BatchPlagiarismRequest;
import org.codeDuplicateChecking.Agent.service.PlagiarismAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 代码查重智能分析控制器
 * 提供API接口让用户能够使用千问增强的代码查重分析功能
 */
@RestController
@RequestMapping("/api/v1/plagiarism/analysis")
public class PlagiarismAnalysisController {

    @Autowired
    private PlagiarismAnalysisService analysisService;
    
    /**
     * 通义千问API密钥
     */
    @Value("${dashscope.api.key:}")
    private String qwenApiKey;
    
    /**
     * AI模型类型
     */
    @Value("${dashscope.model:qwen-plus}")
    private String qwenModel;
    
    /**
     * AI提示词配置
     */
    @Autowired
    private AIPromptConfig aiPromptConfig;

    /**
     * 检查AI连接状态
     */
    @PostMapping("/check-connection")
    public ResponseEntity<Map<String, Object>> checkConnection(@RequestBody ConnectionCheckRequest request) {
        String apiKey = request.getApiKey();
        String model = request.getModel();
        
        // 如果未提供API Key或模型，使用默认值
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = qwenApiKey;
        }
        if (model == null || model.isEmpty()) {
            model = qwenModel;
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 使用配置类中的提示词
            String assistantPrompt = aiPromptConfig.getPrompts().getPlagiarism().getAssistant();
            QwenAgent agent = new QwenAgent(apiKey, model, assistantPrompt);
            
            // 检查连接
            boolean connected = agent.checkConnection();
            response.put("connected", connected);
            response.put("message", connected ? "AI助手连接成功" : "AI助手连接失败");
            
            return ResponseEntity.ok(response);
        } catch (TimeoutException e) {
            response.put("connected", false);
            response.put("message", "AI助手连接超时");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("connected", false);
            response.put("message", "AI助手连接失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 分析两段代码的相似度并提供AI增强分析
     */
    @PostMapping("/compare")
    public ResponseEntity<PlagiarismAnalysisService.PlagiarismAnalysis> compareAndAnalyze(
            @RequestBody SinglePlagiarismRequest request) {
        
        // 确保请求参数有效
        if (request == null || request.getCodeBlock1() == null || request.getCodeBlock2() == null) {
            throw new IllegalArgumentException("请求中必须包含两个有效的代码块");
        }
        
        // 使用请求中的阈值，已经通过lombok设置了默认值0.75
        double threshold = request.getThreshold();
        
        // 执行智能分析
        PlagiarismAnalysisService.PlagiarismAnalysis analysis = 
            analysisService.getSmartPlagiarismAnalysis(
                request.getCodeBlock1(), 
                request.getCodeBlock2(), 
                threshold,
                request.getApiKey(),
                request.getModel());
                
        // 确保分析结果不为空
        if (analysis == null) {
            return ResponseEntity.status(500).body(null);
        }
        
        return ResponseEntity.ok(analysis);
    }

    /**
     * 批量分析多个代码块并提供综合报告
     */
    @PostMapping("/batch")
    public ResponseEntity<PlagiarismAnalysisService.BatchPlagiarismAnalysis> batchAnalyze(
            @RequestBody BatchPlagiarismRequest request) {
        
        // 确保请求参数有效
        if (request == null || request.getCodeBlocks() == null || request.getCodeBlocks().size() < 2) {
            throw new IllegalArgumentException("请求中必须包含至少两个有效的代码块");
        }
        
        // 使用请求中的阈值，已经通过lombok设置了默认值0.75
        double threshold = request.getThreshold();
        
        // 执行批量智能分析
        PlagiarismAnalysisService.BatchPlagiarismAnalysis analysis = 
            analysisService.getBatchSmartAnalysis(
                request.getCodeBlocks(), 
                threshold,
                request.getApiKey(),
                request.getModel());
            
        // 确保分析结果不为空
        if (analysis == null) {
            return ResponseEntity.status(500).body(null);
        }
        
        return ResponseEntity.ok(analysis);
    }

    /**
     * 获取代码改进建议
     * 针对被检测为可能抄袭的代码提供改进建议
     */
    @PostMapping("/improvement")
    public ResponseEntity<Map<String, String>> getImprovementSuggestions(
            @RequestBody ImprovementRequest request) {
        
        try {
            // 获取智能分析结果
            PlagiarismAnalysisService.PlagiarismAnalysis analysis = 
                analysisService.getSmartPlagiarismAnalysis(
                    request.getOriginalCode(), 
                    request.getSuspiciousCode(), 
                    0.5, // 使用较低阈值以获取更多可能的建议
                    request.getApiKey(),
                    request.getModel());
                    
            // 从AI分析中提取改进建议
            String improvementText = "代码改进建议:\n\n";
            if (analysis.getAIEnhancedAnalysis() != null) {
                // 简单提取改进建议部分
                // 实际应用中可能需要更复杂的处理或专门的AI提示来获取改进建议
                improvementText += extractImprovementSuggestions(analysis.getAIEnhancedAnalysis());
            } else {
                improvementText += "系统无法获取AI增强的改进建议，请稍后再试。";
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("suggestions", improvementText);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取改进建议时发生错误: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 简单提取文本中的改进建议部分
     */
    private String extractImprovementSuggestions(String aiText) {
        // 查找包含改进建议的段落
        // 这里使用简单的逻辑，实际应用中可以使用更复杂的NLP方法
        int suggestionIndex = aiText.toLowerCase().indexOf("改进建议");
        if (suggestionIndex != -1) {
            return aiText.substring(suggestionIndex);
        } else if (aiText.contains("建议")) {
            return aiText.substring(aiText.indexOf("建议"));
        } else {
            return "\n基于当前分析，以下是一些一般性建议:\n" +
                  "1. 重新思考算法实现方式\n" +
                  "2. 使用不同的数据结构\n" +
                  "3. 优化代码结构和命名\n" +
                  "4. 添加适当的注释和文档\n" +
                  "5. 实现自己独特的优化逻辑";
        }
    }
    
    /**
     * AI连接检查请求类
     */
    public static class ConnectionCheckRequest {
        private String apiKey;
        private String model;
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
    }
}