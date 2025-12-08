package org.codeDuplicateChecking.Agent.model;

/**
 * 代码改进建议请求模型
 */
public class ImprovementRequest {
    private CodeBlock originalCode; // 原始参考代码
    private CodeBlock suspiciousCode; // 可能存在抄袭的代码
    private String focusArea; // 可选的改进重点领域（如算法、结构、命名等）
    
    // 默认构造函数
    public ImprovementRequest() {}
    
    // 构造函数
    public ImprovementRequest(CodeBlock originalCode, CodeBlock suspiciousCode) {
        this.originalCode = originalCode;
        this.suspiciousCode = suspiciousCode;
    }
    
    // Getter and Setter方法
    public CodeBlock getOriginalCode() {
        return originalCode;
    }
    
    public void setOriginalCode(CodeBlock originalCode) {
        this.originalCode = originalCode;
    }
    
    public CodeBlock getSuspiciousCode() {
        return suspiciousCode;
    }
    
    public void setSuspiciousCode(CodeBlock suspiciousCode) {
        this.suspiciousCode = suspiciousCode;
    }
    
    public String getFocusArea() {
        return focusArea;
    }
    
    public void setFocusArea(String focusArea) {
        this.focusArea = focusArea;
    }
}
