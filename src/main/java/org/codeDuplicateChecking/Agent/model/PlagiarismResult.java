package org.codeDuplicateChecking.Agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码查重结果模型类，表示两个代码块之间的查重结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlagiarismResult {
    // 第一个代码块的信息
    private String codeBlockId1;
    private String author1;
    private String title1;
    
    // 第二个代码块的信息
    private String codeBlockId2;
    private String author2;
    private String title2;
    
    // 相似度得分，范围[0,1]，值越大表示相似度越高
    private double similarityScore;
    
    // 是否判定为抄袭
    private boolean isPlagiarism;
    
    // 抄袭判定阈值
    private double threshold;
    
    // 详细分析说明（可选）
    private String analysis;
}
