package org.codeDuplicateChecking.Agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单对代码块查重请求模型类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SinglePlagiarismRequest {
    // 第一个代码块
    private CodeBlock codeBlock1;
    
    // 第二个代码块
    private CodeBlock codeBlock2;
    
    // 查重阈值，范围[0,1]
    private double threshold = 0.75; // 默认阈值为0.75
    
    // AI API密钥
    private String apiKey;
    
    // AI模型类型
    private String model;
}