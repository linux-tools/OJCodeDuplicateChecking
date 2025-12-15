package org.codeDuplicateChecking.Agent.controller;

import org.codeDuplicateChecking.Agent.model.BatchPlagiarismResult;
import org.codeDuplicateChecking.Agent.model.CodeBlock;
import org.codeDuplicateChecking.Agent.model.PlagiarismRequest;
import org.codeDuplicateChecking.Agent.model.PlagiarismResult;
import org.codeDuplicateChecking.Agent.service.CodePlagiarismService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码查重控制器，处理代码查重相关的HTTP请求
 */
@RestController
@RequestMapping("/api/v1/plagiarism")
public class PlagiarismController {

    private final CodePlagiarismService plagiarismService;

    public PlagiarismController(CodePlagiarismService plagiarismService) {
        this.plagiarismService = plagiarismService;
    }

    /**
     * 比较两个代码块的相似度
     * @param request 包含两个代码块和阈值的请求体
     * @return 查重结果
     */
    @PostMapping("/compare/two")
    public ResponseEntity<PlagiarismResult> compareTwoCodeBlocks(@RequestBody Map<String, Object> request) {
        try {
            // 从请求中提取代码块信息
            // 安全地获取和转换代码块信息
            Map<String, Object> codeBlock1Map = new HashMap<>();
            Map<String, Object> codeBlock2Map = new HashMap<>();
            
            // 安全地处理codeBlock1
            Object block1Obj = request.get("codeBlock1");
            if (block1Obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedBlock1 = (Map<String, Object>) block1Obj;
                codeBlock1Map.putAll(typedBlock1);
            }
            
            // 安全地处理codeBlock2
            Object block2Obj = request.get("codeBlock2");
            if (block2Obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedBlock2 = (Map<String, Object>) block2Obj;
                codeBlock2Map.putAll(typedBlock2);
            }
            
            // 安全地处理threshold
            double threshold = 0.7; // 默认值
            Object thresholdObj = request.get("threshold");
            if (thresholdObj instanceof Number) {
                threshold = ((Number) thresholdObj).doubleValue();
            }

            // 构建代码块对象
            CodeBlock codeBlock1 = buildCodeBlockFromMap(codeBlock1Map);
            CodeBlock codeBlock2 = buildCodeBlockFromMap(codeBlock2Map);

            // 调用服务层进行比较
            PlagiarismResult result = plagiarismService.compareTwoCodeBlocks(codeBlock1, codeBlock2, threshold);
            
            // 确保结果不为空
            if (result == null) {
                return ResponseEntity.status(500).body(null);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Error-Message", e.getMessage())
                .build();
        }
    }

    /**
     * 批量比较多个代码块之间的相似度
     * @param request 包含代码块列表和阈值的请求对象
     * @return 批量查重结果
     */
    @PostMapping("/compare/batch")
    public ResponseEntity<BatchPlagiarismResult> compareMultipleCodeBlocks(@RequestBody PlagiarismRequest request) {
        try {
            // 验证请求参数
            if (request.getCodeBlocks() == null || request.getCodeBlocks().size() < 2) {
                return ResponseEntity.badRequest()
                    .header("X-Error-Message", "至少需要两个代码块进行比较")
                    .build();
            }

            // 调用服务层进行批量比较
            BatchPlagiarismResult result = plagiarismService.compareMultipleCodeBlocks(
                request.getCodeBlocks(), request.getThreshold());
            
            // 确保结果不为空
            if (result == null) {
                return ResponseEntity.status(500).body(null);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Error-Message", e.getMessage())
                .build();
        }
    }

    /**
     * 获取系统支持的编程语言列表
     * @return 支持的语言列表
     */
    @GetMapping("/languages")
    public ResponseEntity<List<String>> getSupportedLanguages() {
        List<String> languages = new ArrayList<>();
        languages.add("Java");
        languages.add("Python");
        languages.add("C++");
        languages.add("C");
        languages.add("C#");
        languages.add("JavaScript");
        languages.add("TypeScript");
        languages.add("PHP");
        languages.add("Ruby");
        languages.add("Go");
        languages.add("Swift");
        languages.add("Kotlin");
        languages.add("Rust");
        languages.add("Scala");
        languages.add("HTML");
        languages.add("CSS");
        return ResponseEntity.ok(languages);
    }

    /**
     * 获取默认查重配置
     * @return 默认配置信息
     */
    @GetMapping("/config/default")
    public ResponseEntity<Map<String, Object>> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("defaultThreshold", 0.7);
        config.put("minThreshold", 0.0);
        config.put("maxThreshold", 1.0);
        config.put("recommendedThreshold", 0.7);
        return ResponseEntity.ok(config);
    }

    /**
     * 从Map对象构建CodeBlock实例
     * @param map 包含代码块信息的Map
     * @return CodeBlock对象
     */
    private CodeBlock buildCodeBlockFromMap(Map<String, Object> map) {
        CodeBlock codeBlock = new CodeBlock();
        
        // 安全地设置各个属性
        setStringProperty(map, "id", codeBlock::setId);
        setStringProperty(map, "code", codeBlock::setCode);
        setStringProperty(map, "author", codeBlock::setAuthor);
        setStringProperty(map, "title", codeBlock::setTitle);
        setStringProperty(map, "language", codeBlock::setLanguage);
        setStringProperty(map, "timestamp", codeBlock::setTimestamp);
        
        return codeBlock;
    }
    
    /**
     * 安全地从Map中获取字符串属性并设置到目标对象
     * @param map 源Map
     * @param key 属性键名
     * @param setter 属性设置器函数
     */
    private void setStringProperty(Map<String, Object> map, String key, java.util.function.Consumer<String> setter) {
        if (map != null && map.containsKey(key)) {
            Object value = map.get(key);
            if (value != null) {
                setter.accept(value.toString());
            }
        }
    }
}
