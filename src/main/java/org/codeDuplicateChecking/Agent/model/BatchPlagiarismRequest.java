package org.codeDuplicateChecking.Agent.model;

import java.util.List;

/**
 * 批量代码查重请求模型
 */
public class BatchPlagiarismRequest {
    private List<CodeBlock> codeBlocks; // 代码块列表
    private Double threshold; // 抄袭检测阈值（可选）
    private String apiKey; // AI API密钥
    private String model; // AI模型类型
    
    // 默认构造函数
    public BatchPlagiarismRequest() {}
    
    // 构造函数
    public BatchPlagiarismRequest(List<CodeBlock> codeBlocks) {
        this.codeBlocks = codeBlocks;
    }
    
    // Getter and Setter方法
    public List<CodeBlock> getCodeBlocks() {
        return codeBlocks;
    }
    
    public void setCodeBlocks(List<CodeBlock> codeBlocks) {
        this.codeBlocks = codeBlocks;
    }
    
    public Double getThreshold() {
        return threshold;
    }
    
    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

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