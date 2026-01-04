package org.codeDuplicateChecking.Agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量代码查重结果模型类，表示一组代码块之间的查重结果集合
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchPlagiarismResult {
    // 查重结果列表
    private List<PlagiarismResult> results;
    
    // 总查重对数
    private int totalPairs;
    
    // 检测出的抄袭对数
    private int plagiarismPairs;
    
    // 最大相似度得分
    private double maxSimilarityScore;
    
    // 平均相似度得分
    private double avgSimilarityScore;
    
    // 查重阈值
    private double threshold;
    
    // 查重时间（毫秒）
    private long processingTimeMs;
    
    // 查重统计信息
    private String statistics;
    
    // 获取代码块总数的辅助方法（不是直接存储的字段，通过结果集计算）
    public int getTotalCodeBlocks() {
        // 通过结果中的唯一代码块ID统计代码块总数
        if (results == null || results.isEmpty()) {
            return 0;
        }
        // 简单返回结果数+1作为估计值（每对比较产生一个结果）
        return (int)Math.ceil(Math.sqrt(results.size() * 2));
    }
}
