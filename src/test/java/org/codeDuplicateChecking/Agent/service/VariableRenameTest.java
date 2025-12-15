package org.codeDuplicateChecking.Agent.service;

import org.codeDuplicateChecking.Agent.model.CodeBlock;
import org.codeDuplicateChecking.Agent.model.PlagiarismResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.codeDuplicateChecking.TestConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestConfig.class)
public class VariableRenameTest {

    @Autowired
    private CodePlagiarismService plagiarismService;

    private String originalCode;
    private String variableRenamedCode;

    @BeforeEach
    void setUp() {
        // 原始代码
        originalCode = "#include <stdio.h> \n" +
                "#include <math.h> \n" +
                "\n" +
                "int main() \n" +
                "{ \n" +
                "\t int s,v,h=8; \n" +
                "\t scanf(\"%d %d\",&s,&v); \n" +
                "\t if(s/v == 0); \n" +
                "\t int t = ceil(1.0*s/v) + 10; \n" +
                "\t if (t <= 480)//小于480分钟 \n" +
                "\t { \n" +
                "\t\t t = 480 - t; \n" +
                "\t\t h = t / 60; \n" +
                "\t\t t = t % 60; \n" +
                "\t } \n" +
                "\t else if (t > 480)//超过480分钟 \n" +
                "\t { \n" +
                "\t\t t = 1920 - t; \n" +
                "\t\t h = t / 60; \n" +
                "\t\t t = t % 60; \n" +
                "\t } \n" +
                "\t printf(\"%02d:%02d\",h,t); \n" +
                "}\n";

        // 变量名修改后的代码
        variableRenamedCode = "#include <stdio.h> \n" +
                "#include <math.h> \n" +
                "\n" +
                "int main() \n" +
                "{ \n" +
                "\t int a,b,c=8; \n" +
                "\t scanf(\"%d %d\",&a,&b); \n" +
                "\t if(a/b == 0); \n" +
                "\t int d = ceil(1.0*a/b) + 10; \n" +
                "\t if (d <= 480) \n" +
                "\t { \n" +
                "\t\t d = 480 - d; \n" +
                "\t\t c = d / 60; \n" +
                "\t\t d = d % 60; \n" +
                "\t } \n" +
                "\t else if (d > 480) \n" +
                "\t { \n" +
                "\t\t d = 1920 - d; \n" +
                "\t\t c = d / 60; \n" +
                "\t\t d = d % 60; \n" +
                "\t } \n" +
                "\t printf(\"%02d:%02d\",c,d); \n" +
                "}\n";
    }

    @Test
    void testVariableRenamePlagiarism() {
        // 创建代码块对象
        CodeBlock originalBlock = new CodeBlock();
        originalBlock.setId("original");
        originalBlock.setCode(originalCode);
        originalBlock.setLanguage("C");
        originalBlock.setTitle("原始代码");
        originalBlock.setAuthor("Original Author");

        CodeBlock renamedBlock = new CodeBlock();
        renamedBlock.setId("renamed");
        renamedBlock.setCode(variableRenamedCode);
        renamedBlock.setLanguage("C");
        renamedBlock.setTitle("变量名修改代码");
        renamedBlock.setAuthor("Another Author");

        // 使用默认阈值0.7进行比较
        PlagiarismResult result = plagiarismService.compareTwoCodeBlocks(originalBlock, renamedBlock, 0.7);

        // 打印结果进行分析
        System.out.println("\n=== 变量名修改抄袭检测测试 ===");
        System.out.println("相似度分数: " + result.getSimilarityScore());
        System.out.println("是否判定为抄袭: " + result.isPlagiarism());
        System.out.println("使用的阈值: " + result.getThreshold());
        
        // 分析代码逻辑
        System.out.println("\n=== 代码逻辑分析 ===");
        System.out.println("1. 两段代码的核心逻辑完全相同，只是变量名被替换:");
        System.out.println("   - s → a, v → b, h → c, t → d");
        System.out.println("2. 代码功能分析:");
        System.out.println("   - 输入两个整数 s/a 和 v/b");
        System.out.println("   - 计算时间：ceil(s/v) + 10 分钟");
        System.out.println("   - 根据总时间判断是前一天还是当天的时间");
        System.out.println("   - 输出格式化的时间 (HH:MM 格式)");
        System.out.println("3. 代码中存在的问题:");
        System.out.println("   - if(s/v == 0); 语句后有分号，导致条件判断无效");
        
        // 验证相似度分数是否足够高以检测出抄袭
        assertTrue(result.getSimilarityScore() >= 0.8, "相似度分数应该足够高以检测出变量名修改的抄袭");
        assertTrue(result.isPlagiarism(), "系统应该将变量名修改的代码判定为抄袭");
    }

    @Test
    void analyzeCodeLogic() {
        // 分析代码的实际逻辑和可能的输出
        System.out.println("\n=== 代码功能详细分析 ===");
        
        // 代码逻辑解释
        System.out.println("这段代码的目的是计算到达时间，并以HH:MM格式输出:");
        System.out.println("1. 输入参数：");
        System.out.println("   - s/a: 距离（单位未明确定义）");
        System.out.println("   - v/b: 速度（单位未明确定义）");
        System.out.println("\n2. 时间计算:");
        System.out.println("   - 计算基础时间: ceil(s/v) 分钟");
        System.out.println("   - 加上额外10分钟: ceil(s/v) + 10 分钟");
        System.out.println("   - 注意：if(s/v == 0); 这行代码有语法问题，分号使条件判断无效");
        System.out.println("\n3. 时间处理逻辑:");
        System.out.println("   - 如果总时间 ≤ 480分钟(8小时): 从8:00往前推");
        System.out.println("   - 如果总时间 > 480分钟: 计算到前一天的时间（1920=24*80分钟）");
        System.out.println("   - 最终转换为小时和分钟并格式化输出");
        System.out.println("\n4. 输入输出示例:");
        System.out.println("   - 输入: 30 10 → 30/10=3分钟 +10分钟=13分钟 → 8:00-13分钟=7:47");
        System.out.println("   - 输出: 07:47");
        System.out.println("   - 输入: 500 1 → 500分钟 +10分钟=510分钟 >480 → 1920-510=1410分钟=23:30");
        System.out.println("   - 输出: 23:30");
        
        // 两段代码的相似性分析
        System.out.println("\n=== 代码相似性分析 ===");
        System.out.println("1. 结构相似性: 100%（完全相同的代码结构）");
        System.out.println("2. 逻辑相似性: 100%（完全相同的计算逻辑）");
        System.out.println("3. 变量名替换模式:");
        System.out.println("   - s → a, v → b, h → c, t → d");
        System.out.println("4. 注释差异: 第一段有中文注释，第二段没有注释");
        System.out.println("5. 综合判断: 这种变量名替换属于典型的低级抄袭手段");
    }
}