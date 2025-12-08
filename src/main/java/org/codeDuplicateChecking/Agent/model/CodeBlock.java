package org.codeDuplicateChecking.Agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码块模型类，表示需要进行查重的代码片段
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeBlock {
    // 代码块的唯一标识符
    private String id;
    // 代码内容
    private String code;
    // 代码作者
    private String author;
    // 代码提交时间或创建时间
    private String timestamp;
    // 代码标题或描述
    private String title;
    // 代码语言
    private String language;
}
