# 千问AI代码查重系统

## 项目简介

千问AI代码查重系统结合了传统代码相似度检测和AI增强分析功能，能够有效识别各种抄袭手法，包括变量名修改、结构调整、代码片段重组等。系统通过通义千问API提供深度语义分析和改进建议，为代码评估提供全面支持。

## 技术栈

### 后端技术

- **OpenJDK 8(Java 8)** - 主要开发语言
- **Spring Boot 2.7.0** - 后端框架，提供RESTful API服务
- **Maven** - 项目构建和依赖管理
- **通义千问API** - 大语言模型接口，提供AI增强分析能力
- **Python 3.x** - 用于自动化打包脚本

### 核心功能技术

- **N-gram特征提取** - 用于代码特征向量化
- **Jaccard相似度** - 计算特征集合的相似性
- **编辑距离算法** - 计算代码文本的编辑相似度
- **加权综合评分** - 结合多种相似度指标的综合评分机制

### 项目架构

- 采用标准的Spring Boot分层架构：
  - Controller层：处理HTTP请求，提供REST API接口
  - Service层：实现核心业务逻辑
  - Model层：定义数据模型和实体类
  - Config层：管理系统配置
  - Utils层：提供通用工具类

## 主要特性

1. **智能代码查重分析**：支持变量名标准化、结构相似度计算、多维度评分
2. **多种抄袭类型识别**：变量名替换、结构调整、代码片段重组、注释添加/删除
3. **AI增强语义分析**：通过通义千问提供深度语义分析和教育性反馈
4. **批量代码分析能力**：支持多文件批量对比分析
5. **详细的分析报告和改进建议**：提供针对性的优化建议

## 快速开始

### 系统要求

- JDK 1.8或更高版本
- Maven 3.6或更高版本
- 足够的网络连接（用于访问千问API）

### 千问API-KEY配置方法

要使用千问AI的增强分析功能，您需要配置有效的通义千问API密钥：

在项目根目录下的`src/main/resources/application.yml`文件中，添加或修改以下配置：

```yaml
dashscope:
  api:
    key: "您的API密钥"
  model:
    name: "qwen-turbo"
```

### 项目编译与运行

#### 使用Maven直接打包

1. 打开命令行工具，进入项目根目录

   ```bash
   cd d:\IdeaProjects\MyAI
   ```

2. 执行Maven编译命令

   ```bash
   mvn clean package
   ```

3. 运行应用程序

   ```bash
   java -jar target\codeDuplicateChecking-1.0_alpha1.jar
   ```

#### 使用自动化打包脚本

我们提供了一个自动化打包脚本，可以简化打包过程并提供额外的功能：

1. 进入项目根目录

   ```bash
   cd d:\IdeaProjects\MyAI
   ```

2. 执行打包脚本

   ```bash
   python package_project.py
   ```

   脚本参数说明：

   ```bash
   # 查看帮助信息
   python package_project.py --help
   ```

3. 运行应用程序

   ```bash
   java -jar target\codeDuplicateChecking-1.0_alpha1.jar
   ```

运行成功后，应用将在`http://localhost:8080`上提供服务。

## 项目结构

```text
.
├── src/                  # 源代码目录
│   ├── main/             # 主要源码
│   │   ├── java/         # Java源代码
│   │   └── resources/    # 资源文件(包含application.yml)
│   └── test/             # 测试代码
├── target/               # 编译输出目录(包含打包的JAR文件)
├── pom.xml               # Maven项目配置文件
├── package_project.py    # 自动化打包脚本
└── logs/                 # 打包脚本日志目录
```

## API使用说明

### 1. 代码查重核心接口

#### 1.1 两代码块比较接口

**URL**: `/api/v1/plagiarism/compare/two`
**方法**: `POST`
**请求体**:

```json
{
  "codeBlock1": {
    "id": "block1",
    "title": "代码标题1",
    "author": "作者1",
    "language": "Java",
    "code": "public class Test { ... }"
  },
  "codeBlock2": {
    "id": "block2",
    "title": "代码标题2",
    "author": "作者2",
    "language": "Java",
    "code": "public class Demo { ... }"
  },
  "threshold": 0.7
}
```

#### 1.2 批量代码比较接口

**URL**: `/api/v1/plagiarism/compare/batch`
**方法**: `POST`
**请求体**:

```json
{
  "codeBlocks": [ /* 多个代码块 */ ],
  "threshold": 0.7,
  "needDetailedAnalysis": true
}
```

#### 1.3 获取支持的编程语言列表

**URL**: `/api/v1/plagiarism/languages`
**方法**: `GET`
**响应体**:

```json
[
  "Java", "Python", "C++", "C", "C#", 
  "JavaScript", "TypeScript", "PHP", "Ruby", 
  "Go", "Swift", "Kotlin", "Rust", "Scala", 
  "HTML", "CSS"
]
```

#### 1.4 获取默认查重配置

**URL**: `/api/v1/plagiarism/config/default`
**方法**: `GET`
**响应体**:

```json
{
  "defaultThreshold": 0.7,
  "minThreshold": 0.0,
  "maxThreshold": 1.0,
  "recommendedThreshold": 0.7
}
```

### 2. AI增强分析接口

#### 2.1 AI增强的两段代码比较分析

**URL**: `/api/plagiarism/analysis/compare`
**方法**: `POST`
**请求体**:

```json
{
  "codeBlock1": { /* 第一段代码块 */ },
  "codeBlock2": { /* 第二段代码块 */ },
  "threshold": 0.75
}
```

#### 2.2 批量AI增强分析

**URL**: `/api/plagiarism/analysis/batch`
**方法**: `POST`
**请求体**:

```json
{
  "codeBlocks": [ /* 多个代码块 */ ],
  "threshold": 0.75
}
```

#### 2.3 获取代码改进建议

**URL**: `/api/plagiarism/analysis/improvement`
**方法**: `POST`
**请求体**:

```json
{
  "originalCode": { /* 原始代码块 */ },
  "suspiciousCode": { /* 可疑代码块 */ }
}
```

### 3. 千问AI对话接口

#### 3.1 传统对话接口

**URL**: `/api/v1/chat/text`
**方法**: `POST`
**请求体**:

```json
{
  "message": "你的问题或指令",
  "systemPrompt": "可选的系统提示词"
}
```

#### 3.2 流式对话接口

**URL**: `/api/v1/chat/stream`
**方法**: `POST`
**请求体**:

```json
{
  "message": "你的问题或指令",
  "systemPrompt": "可选的系统提示词"
}
```

## 技术原理

### 1. 代码预处理

- 移除注释和空白字符
- 变量名标准化（替换为占位符）
- 代码结构特征提取

### 2. 相似度计算

- **Jaccard相似度**：基于n-gram的文本相似度
- **编辑距离**：计算代码序列的编辑操作数
- **结构相似度**：分析代码语法结构的相似性
- **加权融合**：多维度评分的加权组合

### 3. AI增强分析

使用千问AI提供：

- **代码语义层面的深度分析**：理解代码的含义和功能
- **教育性反馈和改进建议**：为开发者提供代码质量和优化方向
- **抄袭模式的详细识别**：识别不同类型的抄袭行为

## 建议阈值设置

- **0.9及以上**：极高相似度，几乎可以确定为直接复制
- **0.7-0.9**：高度相似，可能存在大量复制或改写
- **0.5-0.7**：中度相似，需要进一步人工审查
- **0.3-0.5**：低度相似，可能有共同的实现思路
- **0.3以下**：极低相似度，基本可以确定为独立实现

## 注意事项

1. 代码查重结果仅供参考，建议重要场景下进行人工复核
2. 对于短代码或常见算法实现，可能会出现较高的相似度
3. 支持的编程语言有限，其他语言的查重准确度可能较低
4. 复杂项目的代码查重需要考虑更多因素，如项目结构、设计模式等

## 许可证

本项目采用MIT许可证。详细条款请参阅项目根目录中的[LICENSE文件](LICENSE)。

MIT License

Copyright (c) 2024 AITA Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
