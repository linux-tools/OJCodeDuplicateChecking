package org.codeDuplicateChecking.Agent.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 代码查重工具类，提供代码相似度计算相关功能
 */
public class CodePlagiarismUtils {

    // 移除代码中的注释和空白字符的正则表达式
    private static final Pattern SINGLE_LINE_COMMENT_PATTERN = Pattern.compile("//.*");
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*[\\s\\S]*?\\*/");
    private static final Pattern BLANK_LINES_PATTERN = Pattern.compile("\\n\\s*\\n");
    // 匹配C/C++/Java等语言的变量名的正则表达式
    private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b(?=\\s*[=;,]|[\\s\\(])");

    /**
     * 预处理代码，移除注释、空白行等不影响逻辑的部分，并标准化变量名
     * @param code 原始代码
     * @return 预处理后的代码
     */
    public static String preprocessCode(String code) {
        if (code == null) {
            return "";
        }

        // 移除多行注释
        String noMultiLineComments = MULTI_LINE_COMMENT_PATTERN.matcher(code).replaceAll("");
        
        // 移除单行注释
        String noComments = SINGLE_LINE_COMMENT_PATTERN.matcher(noMultiLineComments).replaceAll("");
        
        // 移除多余的空白行，保留单行空行
        String noBlankLines = BLANK_LINES_PATTERN.matcher(noComments).replaceAll("\n");
        
        // 标准化变量名，将所有变量名替换为统一的占位符
        String normalizedCode = normalizeVariableNames(noBlankLines);
        
        // 移除前导和尾随空白
        return normalizedCode.trim();
    }
    
    /**
     * 标准化代码中的变量名，将所有自定义变量名替换为统一的占位符
     * @param code 预处理后的代码
     * @return 变量名标准化后的代码
     */
    private static String normalizeVariableNames(String code) {
        // 常用的关键字和标准库函数名列表，这些不应该被替换
        Set<String> keywords = new HashSet<>(Arrays.asList(
            "int", "double", "float", "char", "void", "bool", "if", "else", "for", "while", 
            "do", "switch", "case", "default", "return", "break", "continue", "class", 
            "struct", "public", "private", "protected", "static", "const", "namespace", 
            "using", "namespace", "include", "stdio", "math", "main", "printf", "scanf", 
            "ceil", "floor", "abs", "sqrt", "sin", "cos", "tan", "true", "false", 
            "NULL", "nullptr", "new", "delete", "this", "try", "catch", "throw"
        ));
        
        Map<String, String> variableMap = new HashMap<>();
        Matcher matcher = VARIABLE_NAME_PATTERN.matcher(code);
        StringBuilder result = new StringBuilder(code);
        int offset = 0; // 记录替换导致的偏移量
        int varCounter = 0;
        
        while (matcher.find()) {
            String varName = matcher.group();
            // 跳过关键字和标准函数名
            if (!keywords.contains(varName)) {
                variableMap.putIfAbsent(varName, "VAR_" + (varCounter++));
                String replacement = variableMap.get(varName);
                
                // 更新结果字符串
                result.replace(matcher.start() + offset, matcher.end() + offset, replacement);
                offset += replacement.length() - varName.length();
            }
        }
        
        return result.toString();
    }

    /**
     * 将代码分割成n-gram标记
     * @param code 预处理后的代码
     * @param n n-gram的大小
     * @return n-gram标记集合
     */
    public static Set<String> generateNGrams(String code, int n) {
        Set<String> nGrams = new HashSet<>();
        if (code.length() < n) {
            return nGrams;
        }

        for (int i = 0; i <= code.length() - n; i++) {
            nGrams.add(code.substring(i, i + n));
        }
        return nGrams;
    }

    /**
     * 使用Jaccard相似度计算两个代码块的相似度
     * @param code1 第一个代码块
     * @param code2 第二个代码块
     * @param n n-gram的大小
     * @return 相似度值，范围[0,1]，值越大表示相似度越高
     */
    public static double calculateJaccardSimilarity(String code1, String code2, int n) {
        // 预处理代码
        String processedCode1 = preprocessCode(code1);
        String processedCode2 = preprocessCode(code2);

        // 生成n-gram集合
        Set<String> nGrams1 = generateNGrams(processedCode1, n);
        Set<String> nGrams2 = generateNGrams(processedCode2, n);

        // 计算交集大小
        Set<String> intersection = new HashSet<>(nGrams1);
        intersection.retainAll(nGrams2);

        // 计算并集大小
        Set<String> union = new HashSet<>(nGrams1);
        union.addAll(nGrams2);

        // 计算Jaccard相似度：交集大小 / 并集大小
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    /**
     * 计算编辑距离（Levenshtein距离）
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return 编辑距离值
     */
    public static int calculateEditDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        // 创建DP表格
        int[][] dp = new int[m + 1][n + 1];
        
        // 初始化第一行和第一列
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        // 填充DP表格
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }

    /**
     * 使用编辑距离计算两个代码块的相似度
     * @param code1 第一个代码块
     * @param code2 第二个代码块
     * @return 相似度值，范围[0,1]，值越大表示相似度越高
     */
    public static double calculateEditDistanceSimilarity(String code1, String code2) {
        // 预处理代码
        String processedCode1 = preprocessCode(code1);
        String processedCode2 = preprocessCode(code2);
        
        // 计算编辑距离
        int distance = calculateEditDistance(processedCode1, processedCode2);
        
        // 计算最大长度
        int maxLength = Math.max(processedCode1.length(), processedCode2.length());
        
        // 转换为相似度
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }

    /**
     * 计算两个代码块的综合相似度，结合多种相似度算法
     * @param code1 第一个代码块
     * @param code2 第二个代码块
     * @return 综合相似度值，范围[0,1]，值越大表示相似度越高
     */
    public static double calculatePlagiarismScore(String code1, String code2) {
        // 使用不同的n值计算Jaccard相似度
        double jaccardSimilarity4 = calculateJaccardSimilarity(code1, code2, 4);
        double jaccardSimilarity8 = calculateJaccardSimilarity(code1, code2, 8);
        
        // 计算编辑距离相似度
        double editDistanceSimilarity = calculateEditDistanceSimilarity(code1, code2);
        
        // 计算结构相似度 - 这对变量名修改的情况特别有效
        double structureSimilarity = calculateStructureSimilarity(code1, code2);
        
        // 加权平均得到综合相似度
        // 增加结构相似度权重，减少编辑距离权重，提高对变量名修改抄袭的检测能力
        return 0.2 * jaccardSimilarity4 + 0.2 * jaccardSimilarity8 + 0.3 * editDistanceSimilarity + 0.3 * structureSimilarity;
    }
    
    /**
     * 计算两个代码块的结构相似度，重点关注代码的结构而不是具体的变量名
     * @param code1 第一个代码块
     * @param code2 第二个代码块
     * @return 结构相似度值，范围[0,1]，值越大表示结构越相似
     */
    private static double calculateStructureSimilarity(String code1, String code2) {
        // 预处理代码（已经包含了变量名标准化）
        String processedCode1 = preprocessCode(code1);
        String processedCode2 = preprocessCode(code2);
        
        // 提取代码结构特征：操作符、控制结构等
        List<String> features1 = extractStructureFeatures(processedCode1);
        List<String> features2 = extractStructureFeatures(processedCode2);
        
        // 计算特征序列的编辑距离
        int distance = calculateSequenceEditDistance(features1, features2);
        int maxLength = Math.max(features1.size(), features2.size());
        
        // 转换为相似度
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }
    
    /**
     * 提取代码的结构特征
     * @param code 预处理后的代码
     * @return 结构特征列表
     */
    private static List<String> extractStructureFeatures(String code) {
        List<String> features = new ArrayList<>();
        
        // 定义需要提取的结构特征
        String[] structureTokens = {
            "if", "else", "for", "while", "do", "switch", "case", "default", "return",
            "{", "}", "(", ")", "[", "]", "=", "+=", "-=", "*=", "/=",
            "+", "-", "*", "/", "%", "<", ">", "<=", ">=", "==", "!=", "&&", "||", "!",
            ";", ",", "{", "}", "(", ")"
        };
        
        // 标记所有结构特征
        for (String token : structureTokens) {
            int index = 0;
            while ((index = code.indexOf(token, index)) != -1) {
                // 确保这不是其他词的一部分
                if ((index == 0 || !Character.isLetterOrDigit(code.charAt(index - 1))) &&
                    (index + token.length() >= code.length() || !Character.isLetterOrDigit(code.charAt(index + token.length())))) {
                    features.add(token);
                }
                index += token.length();
            }
        }
        
        // 提取控制流模式
        Pattern controlFlowPattern = Pattern.compile("if\\s*\\(|for\\s*\\(|while\\s*\\(|do\\s*\\{|switch\\s*\\(");
        Matcher matcher = controlFlowPattern.matcher(code);
        while (matcher.find()) {
            features.add(matcher.group());
        }
        
        return features;
    }
    
    /**
     * 计算两个序列的编辑距离
     * @param seq1 第一个序列
     * @param seq2 第二个序列
     * @return 编辑距离值
     */
    private static int calculateSequenceEditDistance(List<String> seq1, List<String> seq2) {
        int m = seq1.size();
        int n = seq2.size();
        
        // 创建DP表格
        int[][] dp = new int[m + 1][n + 1];
        
        // 初始化第一行和第一列
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        // 填充DP表格
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (seq1.get(i - 1).equals(seq2.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }

    /**
     * 判断两个代码块是否存在抄袭
     * @param code1 第一个代码块
     * @param code2 第二个代码块
     * @param threshold 抄袭阈值，范围[0,1]，建议值0.7
     * @return 如果相似度超过阈值，则返回true
     */
    public static boolean isPlagiarism(String code1, String code2, double threshold) {
        double similarity = calculatePlagiarismScore(code1, code2);
        return similarity >= threshold;
    }
}
