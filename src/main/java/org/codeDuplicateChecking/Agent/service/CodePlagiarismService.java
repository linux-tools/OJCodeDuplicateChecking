package org.codeDuplicateChecking.Agent.service;

import org.codeDuplicateChecking.Agent.model.BatchPlagiarismResult;
import org.codeDuplicateChecking.Agent.model.CodeBlock;
import org.codeDuplicateChecking.Agent.model.PlagiarismResult;
import org.codeDuplicateChecking.Agent.utils.CodePlagiarismUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 代码查重服务层，提供代码查重相关的业务逻辑
 */
@Service
public class CodePlagiarismService {

    // 默认的抄袭阈值
    private static final double DEFAULT_THRESHOLD = 0.7;
    
    // 线程池配置
    private final ExecutorService executorService;
    
    public CodePlagiarismService() {
        // 初始化线程池，使用CPU核心数的线程
        int processors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(Math.max(2, processors));
    }
    
    /**
     * 比较两个代码块的相似度
     * @param codeBlock1 第一个代码块
     * @param codeBlock2 第二个代码块
     * @param threshold 抄袭阈值
     * @return 查重结果
     */
    public PlagiarismResult compareTwoCodeBlocks(CodeBlock codeBlock1, CodeBlock codeBlock2, double threshold) {
        // 确保阈值在有效范围内
        double validThreshold = Math.max(0.0, Math.min(1.0, threshold));
        if (validThreshold == 0.0) {
            validThreshold = DEFAULT_THRESHOLD;
        }
        
        // 计算相似度
        double similarityScore = CodePlagiarismUtils.calculatePlagiarismScore(
                codeBlock1.getCode(), codeBlock2.getCode());
        
        // 判断是否为抄袭
        boolean isPlagiarism = similarityScore >= validThreshold;
        
        // 生成分析说明
        String analysis = generateAnalysis(similarityScore, validThreshold, codeBlock1.getLanguage());
        
        // 返回查重结果
        return new PlagiarismResult(
                codeBlock1.getId(), codeBlock1.getAuthor(), codeBlock1.getTitle(),
                codeBlock2.getId(), codeBlock2.getAuthor(), codeBlock2.getTitle(),
                similarityScore, isPlagiarism, validThreshold, analysis
        );
    }
    
    /**
     * 批量比较多个代码块之间的相似度
     * @param codeBlocks 代码块列表
     * @param threshold 抄袭阈值
     * @return 批量查重结果
     */
    public BatchPlagiarismResult compareMultipleCodeBlocks(List<CodeBlock> codeBlocks, double threshold) {
        long startTime = System.currentTimeMillis();
        
        List<PlagiarismResult> results = new ArrayList<>();
        int totalPairs = 0;
        int plagiarismPairs = 0;
        double maxSimilarityScore = 0.0;
        double totalSimilarityScore = 0.0;
        
        // 确保代码块列表不为空且至少有两个代码块
        if (codeBlocks != null && codeBlocks.size() >= 2) {
            // 生成所有唯一的代码块对组合
            List<CompletableFuture<PlagiarismResult>> futures = new ArrayList<>();
            
            for (int i = 0; i < codeBlocks.size(); i++) {
                for (int j = i + 1; j < codeBlocks.size(); j++) {
                    final CodeBlock block1 = codeBlocks.get(i);
                    final CodeBlock block2 = codeBlocks.get(j);
                    
                    // 异步执行每对代码块的比较
                    CompletableFuture<PlagiarismResult> future = CompletableFuture.supplyAsync(() -> {
                        return compareTwoCodeBlocks(block1, block2, threshold);
                    }, executorService);
                    
                    futures.add(future);
                }
            }
            
            // 等待所有比较完成并收集结果
            try {
                CompletableFuture<Void> allOf = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));
                
                // 获取所有结果
                List<PlagiarismResult> completedResults = allOf.thenApply(v -> 
                        futures.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList())
                ).get();
                
                results.addAll(completedResults);
                
                // 统计结果
                totalPairs = completedResults.size();
                
                for (PlagiarismResult result : completedResults) {
                    totalSimilarityScore += result.getSimilarityScore();
                    
                    if (result.getSimilarityScore() > maxSimilarityScore) {
                        maxSimilarityScore = result.getSimilarityScore();
                    }
                    
                    if (result.isPlagiarism()) {
                        plagiarismPairs++;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                // 处理异常
                Thread.currentThread().interrupt();
                throw new RuntimeException("Error comparing code blocks in parallel", e);
            }
        }
        
        // 计算平均相似度
        double avgSimilarityScore = totalPairs > 0 ? totalSimilarityScore / totalPairs : 0.0;
        
        // 生成统计信息
        String statistics = String.format(
                "总共比较了 %d 对代码块，发现 %d 对存在潜在抄袭（相似度阈值：%.2f），" +
                "平均相似度：%.2f，最大相似度：%.2f",
                totalPairs, plagiarismPairs, threshold, avgSimilarityScore, maxSimilarityScore
        );
        
        // 计算处理时间
        long processingTimeMs = System.currentTimeMillis() - startTime;
        
        // 返回批量查重结果
        return new BatchPlagiarismResult(
                results, totalPairs, plagiarismPairs, maxSimilarityScore,
                avgSimilarityScore, threshold, processingTimeMs, statistics
        );
    }
    
    /**
     * 生成查重分析说明
     * @param similarityScore 相似度得分
     * @param threshold 抄袭阈值
     * @param language 代码语言
     * @return 分析说明文本
     */
    private String generateAnalysis(double similarityScore, double threshold, String language) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append(String.format("代码相似度分析结果（%s）：", language != null ? language : "未知语言"));
        analysis.append(String.format("\n相似度得分：%.2f/1.00", similarityScore));
        analysis.append(String.format("\n使用阈值：%.2f/1.00", threshold));
        
        // 根据相似度得分给出评估
        if (similarityScore >= threshold) {
            analysis.append("\n评估结果：**存在潜在抄袭**");
            
            if (similarityScore >= 0.9) {
                analysis.append("\n详细说明：两段代码极其相似，高度疑似直接复制或仅做少量修改");
            } else if (similarityScore >= 0.8) {
                analysis.append("\n详细说明：两段代码相似度很高，可能存在大量复制或改写");
            } else {
                analysis.append("\n详细说明：两段代码存在一定程度的相似性，建议进一步人工审查");
            }
        } else {
            analysis.append("\n评估结果：未检测到明显抄袭迹象");
            
            if (similarityScore >= 0.6) {
                analysis.append("\n详细说明：两段代码有一定相似性，但未达到抄袭阈值");
            } else if (similarityScore >= 0.4) {
                analysis.append("\n详细说明：两段代码相似度较低，可能有少量共同的实现模式");
            } else {
                analysis.append("\n详细说明：两段代码相似度很低，独立实现的可能性较大");
            }
        }
        
        return analysis.toString();
    }
    
    /**
     * 关闭线程池
     */
    public void shutdown() {
        if (executorService != null && !executorService.isTerminated()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
