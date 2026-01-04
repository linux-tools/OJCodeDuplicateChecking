package org.codeDuplicateChecking.Agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 代码查重请求模型类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlagiarismRequest {
    // 待对比的代码块列表
    private List<CodeBlock> codeBlocks;
    
    // 查重阈值，范围[0,1]
    private double threshold = 0.7; // 默认阈值为0.7
    
    // 是否需要详细分析
    private boolean needDetailedAnalysis = false;
}
