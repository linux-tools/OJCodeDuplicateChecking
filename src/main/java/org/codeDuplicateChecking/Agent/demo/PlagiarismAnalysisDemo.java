package org.codeDuplicateChecking.Agent.demo;

import org.codeDuplicateChecking.Agent.model.CodeBlock;
import org.codeDuplicateChecking.Agent.service.PlagiarismAnalysisService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 代码查重智能分析演示程序
 * 结合千问Agent展示智能代码抄袭检测功能
 */
@SpringBootApplication
public class PlagiarismAnalysisDemo {

    public static void main(String[] args) {
        // 启动Spring Boot应用
        ConfigurableApplicationContext context = SpringApplication.run(PlagiarismAnalysisDemo.class, args);
        
        // 获取PlagiarismAnalysisService服务
        PlagiarismAnalysisService analysisService = context.getBean(PlagiarismAnalysisService.class);
        
        System.out.println("======= 代码查重智能分析系统演示 =======");
        System.out.println("本系统结合代码查重工具和千问AI，提供代码抄袭智能分析");
        System.out.println("========================================\n");
        
        try {
            // 演示变量名修改的抄袭检测
            demonstrateVariableRenameDetection(analysisService);
            
            // 演示结构变化的抄袭检测
            demonstrateStructureChangeDetection(analysisService);
            
            // 演示批量代码分析
            demonstrateBatchAnalysis(analysisService);
            
        } catch (Exception e) {
            System.out.println("演示过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭Spring Boot应用
            context.close();
        }
    }
    
    /**
     * 演示变量名修改的代码抄袭检测
     */
    private static void demonstrateVariableRenameDetection(PlagiarismAnalysisService service) {
        System.out.println("\n【演示1: 变量名修改检测】");
        System.out.println("分析两段仅有变量名不同的代码...\n");
        
        // 准备代码块1 - 原始代码
        CodeBlock code1 = new CodeBlock();
        code1.setAuthor("原始作者");
        code1.setTitle("变量名标准化前的代码");
        code1.setLanguage("java");
        code1.setCode(
            "// 计算斐波那契数列\n" +
            "public class FibonacciCalculator {\n" +
            "    public static int calculateFibonacci(int n) {\n" +
            "        if (n <= 1) {\n" +
            "            return n;\n" +
            "        }\n" +
            "        int first = 0;\n" +
            "        int second = 1;\n" +
            "        int result = 0;\n" +
            "        \n" +
            "        for (int i = 2; i <= n; i++) {\n" +
            "            result = first + second;\n" +
            "            first = second;\n" +
            "            second = result;\n" +
            "        }\n" +
            "        \n" +
            "        return result;\n" +
            "    }\n" +
            "}\n"
        );
        
        // 准备代码块2 - 变量名修改后的代码
        CodeBlock code2 = new CodeBlock();
        code2.setAuthor("可能的抄袭者");
        code2.setTitle("变量名被修改的代码");
        code2.setLanguage("java");
        code2.setCode(
            "// 计算斐波那契数列\n" +
            "public class FibCalc {\n" +
            "    public static int getFib(int input) {\n" +
            "        if (input <= 1) {\n" +
            "            return input;\n" +
            "        }\n" +
            "        int a = 0;\n" +
            "        int b = 1;\n" +
            "        int output = 0;\n" +
            "        \n" +
            "        for (int counter = 2; counter <= input; counter++) {\n" +
            "            output = a + b;\n" +
            "            a = b;\n" +
            "            b = output;\n" +
            "        }\n" +
            "        \n" +
            "        return output;\n" +
            "    }\n" +
            "}\n"
        );
        
        // 执行分析
        PlagiarismAnalysisService.PlagiarismAnalysis analysis = 
            service.getSmartPlagiarismAnalysis(code1, code2, 0.7);
        
        // 显示结果
        System.out.println("分析结果:");
        System.out.println("  相似度得分: " + analysis.getBaseResult().getSimilarityScore());
        System.out.println("  是否判定为抄袭: " + analysis.getBaseResult().isPlagiarism());
        
        if (analysis.getAIEnhancedAnalysis() != null) {
            System.out.println("\nAI智能分析:");
            // 只显示部分AI分析结果，避免输出过多
            String aiText = analysis.getAIEnhancedAnalysis();
            if (aiText.length() > 200) {
                System.out.println(aiText.substring(0, 200) + "...\n[更多分析内容省略]");
            } else {
                System.out.println(aiText);
            }
        } else if (analysis.getAIError() != null) {
            System.out.println("\nAI分析状态: " + analysis.getAIError());
        } else {
            System.out.println("\nAI分析: 跳过 (API密钥未配置或相似度不高)");
        }
        
        System.out.println("\n按Enter键继续...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
    
    /**
     * 演示结构变化的代码抄袭检测
     */
    private static void demonstrateStructureChangeDetection(PlagiarismAnalysisService service) {
        System.out.println("\n【演示2: 结构变化检测】");
        System.out.println("分析两段结构略有变化但核心逻辑相同的代码...\n");
        
        // 准备代码块1 - 原始代码
        CodeBlock code1 = new CodeBlock();
        code1.setAuthor("原始作者");
        code1.setTitle("使用for循环的代码");
        code1.setLanguage("python");
        code1.setCode(
            "def find_max_value(numbers):\n" +
            "    if not numbers:\n" +
            "        return None\n" +
            "    \n" +
            "    max_num = numbers[0]\n" +
            "    for num in numbers[1:]:\n" +
            "        if num > max_num:\n" +
            "            max_num = num\n" +
            "    \n" +
            "    return max_num\n"
        );
        
        // 准备代码块2 - 结构修改后的代码
        CodeBlock code2 = new CodeBlock();
        code2.setAuthor("可能的抄袭者");
        code2.setTitle("使用while循环重写的代码");
        code2.setLanguage("python");
        code2.setCode(
            "def get_max_element(data_list):\n" +
            "    # 空列表检查\n" +
            "    if len(data_list) == 0:\n" +
            "        return None\n" +
            "    \n" +
            "    # 初始化最大值\n" +
            "    current_max = data_list[0]\n" +
            "    index = 1\n" +
            "    \n" +
            "    # 使用while循环查找最大值\n" +
            "    while index < len(data_list):\n" +
            "        element = data_list[index]\n" +
            "        if element > current_max:\n" +
            "            current_max = element\n" +
            "        index += 1\n" +
            "    \n" +
            "    return current_max\n"
        );
        
        // 执行分析
        PlagiarismAnalysisService.PlagiarismAnalysis analysis = 
            service.getSmartPlagiarismAnalysis(code1, code2, 0.7);
        
        // 显示结果
        System.out.println("分析结果:");
        System.out.println("  相似度得分: " + analysis.getBaseResult().getSimilarityScore());
        System.out.println("  是否判定为抄袭: " + analysis.getBaseResult().isPlagiarism());
        
        if (analysis.getAIEnhancedAnalysis() != null) {
            System.out.println("\nAI智能分析:");
            // 只显示部分AI分析结果
            String aiText = analysis.getAIEnhancedAnalysis();
            if (aiText.length() > 200) {
                System.out.println(aiText.substring(0, 200) + "...\n[更多分析内容省略]");
            } else {
                System.out.println(aiText);
            }
        } else if (analysis.getAIError() != null) {
            System.out.println("\nAI分析状态: " + analysis.getAIError());
        } else {
            System.out.println("\nAI分析: 跳过 (API密钥未配置或相似度不高)");
        }
        
        System.out.println("\n按Enter键继续...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
    
    /**
     * 演示批量代码分析
     */
    private static void demonstrateBatchAnalysis(PlagiarismAnalysisService service) {
        System.out.println("\n【演示3: 批量代码分析】");
        System.out.println("分析多个代码块之间的相似度关系...\n");
        
        // 准备多个代码块
        List<CodeBlock> codeBlocks = new ArrayList<>();
        
        // 代码块1 - 原始代码
        CodeBlock code1 = new CodeBlock();
        code1.setAuthor("学生A");
        code1.setTitle("原始冒泡排序");
        code1.setLanguage("c");
        code1.setCode(
            "#include \"stdio.h\"\n" +
            "\n" +
            "void bubbleSort(int arr[], int n) {\n" +
            "    int i, j, temp;\n" +
            "    for (i = 0; i < n-1; i++) {\n" +
            "        for (j = 0; j < n-i-1; j++) {\n" +
            "            if (arr[j] > arr[j+1]) {\n" +
            "                temp = arr[j];\n" +
            "                arr[j] = arr[j+1];\n" +
            "                arr[j+1] = temp;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
        
        // 代码块2 - 变量名修改后的代码
        CodeBlock code2 = new CodeBlock();
        code2.setAuthor("学生B");
        code2.setTitle("修改变量名的冒泡排序");
        code2.setLanguage("c");
        code2.setCode(
            "#include \"stdio.h\"\n" +
            "\n" +
            "void sortArray(int data[], int size) {\n" +
            "    int x, y, swap;\n" +
            "    for (x = 0; x < size-1; x++) {\n" +
            "        for (y = 0; y < size-x-1; y++) {\n" +
            "            if (data[y] > data[y+1]) {\n" +
            "                swap = data[y];\n" +
            "                data[y] = data[y+1];\n" +
            "                data[y+1] = swap;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
        
        // 代码块3 - 完全不同的代码
        CodeBlock code3 = new CodeBlock();
        code3.setAuthor("学生C");
        code3.setTitle("快速排序实现");
        code3.setLanguage("c");
        code3.setCode(
            "#include \"stdio.h\"\n" +
            "\n" +
            "int partition(int arr[], int low, int high) {\n" +
            "    int pivot = arr[high];\n" +
            "    int i = (low - 1);\n" +
            "    for (int j = low; j < high; j++) {\n" +
            "        if (arr[j] <= pivot) {\n" +
            "            i++;\n" +
            "            int temp = arr[i];\n" +
            "            arr[i] = arr[j];\n" +
            "            arr[j] = temp;\n" +
            "        }\n" +
            "    }\n" +
            "    int temp = arr[i + 1];\n" +
            "    arr[i + 1] = arr[high];\n" +
            "    arr[high] = temp;\n" +
            "    return i + 1;\n" +
            "}\n" +
            "\n" +
            "void quickSort(int arr[], int low, int high) {\n" +
            "    if (low < high) {\n" +
            "        int pi = partition(arr, low, high);\n" +
            "        quickSort(arr, low, pi - 1);\n" +
            "        quickSort(arr, pi + 1, high);\n" +
            "    }\n" +
            "}\n"
        );
        
        codeBlocks.add(code1);
        codeBlocks.add(code2);
        codeBlocks.add(code3);
        
        // 执行批量分析
        PlagiarismAnalysisService.BatchPlagiarismAnalysis analysis = 
            service.getBatchSmartAnalysis(codeBlocks, 0.7);
        
        // 显示结果
        System.out.println("批量分析结果:");
        System.out.println("  检测到的抄袭对数量: " + analysis.getBaseResult().getPlagiarismPairs());
        System.out.println("  代码块总数: " + analysis.getBaseResult().getTotalCodeBlocks());
        
        // 显示详细的抄袭检测结果
        System.out.println("\n详细抄袭检测结果:");
        analysis.getBaseResult().getResults().forEach(result -> {
            System.out.println("  - " + result.getTitle1() + " vs " + result.getTitle2() + 
                             ": 相似度 " + result.getSimilarityScore() + 
                             ", 抄袭判定 " + result.isPlagiarism());
        });
        
        if (analysis.getBatchSummary() != null) {
            System.out.println("\nAI批量分析总结:");
            // 只显示部分AI分析结果
            String aiText = analysis.getBatchSummary();
            if (aiText.length() > 200) {
                System.out.println(aiText.substring(0, 200) + "...\n[更多分析内容省略]");
            } else {
                System.out.println(aiText);
            }
        } else if (analysis.getAIError() != null) {
            System.out.println("\nAI分析状态: " + analysis.getAIError());
        } else {
            System.out.println("\nAI分析: 跳过 (API密钥未配置或未检测到抄袭对)");
        }
    }
}
