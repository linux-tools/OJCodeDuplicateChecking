package org.codeDuplicateChecking.Agent.service;

import org.codeDuplicateChecking.Agent.model.BatchPlagiarismResult;
import org.codeDuplicateChecking.Agent.model.CodeBlock;
import org.codeDuplicateChecking.Agent.model.PlagiarismResult;
import org.codeDuplicateChecking.Agent.utils.CodePlagiarismUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.codeDuplicateChecking.TestConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 代码查重服务测试类
 */
@SpringBootTest(classes = TestConfig.class)
class CodePlagiarismServiceTest {

    @Autowired
    private CodePlagiarismService plagiarismService;

    private CodeBlock similarCode1;
    private CodeBlock similarCode2;
    private CodeBlock differentCode;

    @BeforeEach
    void setUp() {
        // 创建相似的Java代码块
        similarCode1 = new CodeBlock();
        similarCode1.setId("test_block_1");
        similarCode1.setTitle("测试代码1");
        similarCode1.setAuthor("测试用户1");
        similarCode1.setLanguage("Java");
        similarCode1.setCode(
                "public class Solution {\n" +
                "    public int binarySearch(int[] nums, int target) {\n" +
                "        int left = 0;\n" +
                "        int right = nums.length - 1;\n" +
                "        while (left <= right) {\n" +
                "            int mid = left + (right - left) / 2;\n" +
                "            if (nums[mid] == target) {\n" +
                "                return mid;\n" +
                "            } else if (nums[mid] < target) {\n" +
                "                left = mid + 1;\n" +
                "            } else {\n" +
                "                right = mid - 1;\n" +
                "            }\n" +
                "        }\n" +
                "        return -1;\n" +
                "    }\n" +
                "}"
        );

        similarCode2 = new CodeBlock();
        similarCode2.setId("test_block_2");
        similarCode2.setTitle("测试代码2");
        similarCode2.setAuthor("测试用户2");
        similarCode2.setLanguage("Java");
        similarCode2.setCode(
                "public class BinarySearch {\n" + // 类名不同
                "    public static int search(int[] arr, int target) {\n" + // 方法名和参数名不同
                "        int left = 0;\n" +
                "        int right = arr.length - 1;\n" + // 使用arr而不是nums
                "        while (left <= right) {\n" +
                "            int mid = left + (right - left) / 2;\n" + // 相同的中间计算逻辑
                "            if (arr[mid] == target) {\n" +
                "                return mid;\n" +
                "            } else if (arr[mid] < target) {\n" +
                "                left = mid + 1;\n" +
                "            } else {\n" +
                "                right = mid - 1;\n" +
                "            }\n" +
                "        }\n" +
                "        return -1;\n" +
                "    }\n" +
                "}"
        );

        // 创建完全不同的Python代码块
        differentCode = new CodeBlock();
        differentCode.setId("test_block_3");
        differentCode.setTitle("不同的测试代码");
        differentCode.setAuthor("测试用户3");
        differentCode.setLanguage("Python");
        differentCode.setCode(
                "def quick_sort(arr):\n" +
                "    if len(arr) <= 1:\n" +
                "        return arr\n" +
                "    pivot = arr[len(arr) // 2]\n" +
                "    left = [x for x in arr if x < pivot]\n" +
                "    middle = [x for x in arr if x == pivot]\n" +
                "    right = [x for x in arr if x > pivot]\n" +
                "    return quick_sort(left) + middle + quick_sort(right)\n" +
                "\n" +
                "# 测试快速排序\n" +
                "test_array = [3, 6, 8, 10, 1, 2, 1]\n" +
                "sorted_array = quick_sort(test_array)\n" +
                "print(sorted_array)"
        );
    }

    @Test
    void testCompareTwoCodeBlocks_Similar() {
        // 测试两个相似代码块的比较
        PlagiarismResult result = plagiarismService.compareTwoCodeBlocks(similarCode1, similarCode2, 0.7);
        
        assertNotNull(result);
        System.out.println("相似代码块的相似度得分: " + result.getSimilarityScore());
        System.out.println("分析: " + result.getAnalysis());
        
        // 相似代码的相似度应该较高（根据我们的算法实现，预期至少在0.6以上）
        assertTrue(result.getSimilarityScore() >= 0.6);
        // 使用0.7阈值，应该被判定为抄袭
        assertTrue(result.isPlagiarism());
    }

    @Test
    void testCompareTwoCodeBlocks_Different() {
        // 测试两个不同代码块的比较
        PlagiarismResult result = plagiarismService.compareTwoCodeBlocks(similarCode1, differentCode, 0.7);
        
        assertNotNull(result);
        System.out.println("不同代码块的相似度得分: " + result.getSimilarityScore());
        System.out.println("分析: " + result.getAnalysis());
        
        // 不同代码的相似度应该较低（预期在0.3以下）
        assertTrue(result.getSimilarityScore() <= 0.4);
        // 使用0.7阈值，不应该被判定为抄袭
        assertFalse(result.isPlagiarism());
    }

    @Test
    void testCompareMultipleCodeBlocks() {
        // 测试批量比较多个代码块
        List<CodeBlock> codeBlocks = new ArrayList<>();
        codeBlocks.add(similarCode1);
        codeBlocks.add(similarCode2);
        codeBlocks.add(differentCode);
        
        BatchPlagiarismResult result = plagiarismService.compareMultipleCodeBlocks(codeBlocks, 0.7);
        
        assertNotNull(result);
        System.out.println("批量比较统计: " + result.getStatistics());
        System.out.println("结果数量: " + result.getResults().size());
        
        // 三个代码块应该产生3对组合 (3*2/2 = 3)
        assertEquals(3, result.getResults().size());
        
        // 检查每对结果的有效性
        for (PlagiarismResult pairResult : result.getResults()) {
            assertNotNull(pairResult);
            assertTrue(pairResult.getSimilarityScore() >= 0 && pairResult.getSimilarityScore() <= 1);
        }
    }

    @Test
    void testPlagiarismUtils() {
        // 直接测试工具类的相似度计算
        double similarScore = CodePlagiarismUtils.calculatePlagiarismScore(
                similarCode1.getCode(), similarCode2.getCode());
        double differentScore = CodePlagiarismUtils.calculatePlagiarismScore(
                similarCode1.getCode(), differentCode.getCode());
        
        System.out.println("工具类计算 - 相似代码: " + similarScore);
        System.out.println("工具类计算 - 不同代码: " + differentScore);
        
        // 相似代码的分数应该高于不同代码
        assertTrue(similarScore > differentScore);
        // 确保分数在0-1范围内
        assertTrue(similarScore >= 0 && similarScore <= 1);
        assertTrue(differentScore >= 0 && differentScore <= 1);
    }

    @Test
    void testThresholdEffect() {
        // 测试不同阈值对结果的影响
        PlagiarismResult result1 = plagiarismService.compareTwoCodeBlocks(similarCode1, similarCode2, 0.9);
        PlagiarismResult result2 = plagiarismService.compareTwoCodeBlocks(similarCode1, similarCode2, 0.5);
        
        System.out.println("阈值0.9: " + result1.isPlagiarism());
        System.out.println("阈值0.5: " + result2.isPlagiarism());
        
        // 降低阈值应该更容易判定为抄袭
        assertFalse(result1.isPlagiarism() && !result2.isPlagiarism());
    }
}
