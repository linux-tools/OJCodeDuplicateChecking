package org.codeDuplicateChecking.Agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI提示词配置类，用于处理application.yml中的ai相关配置<br>
 * 该类使用Spring Boot的@ConfigurationProperties注解，自动映射配置文件中的ai.*属性<br>
 * 主要用于管理AI分析所需的提示词模板，支持结构化的配置管理
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIPromptConfig {
    
    /**
     * 提示词配置集合，包含各种AI任务的提示词模板
     */
    private Prompts prompts = new Prompts();
    
    /**
     * 获取提示词配置集合
     * @return Prompts对象，包含各类提示词配置
     */
    public Prompts getPrompts() {
        return prompts;
    }
    
    /**
     * 设置提示词配置集合
     * @param prompts 提示词配置集合
     */
    public void setPrompts(Prompts prompts) {
        this.prompts = prompts;
    }
    
    /**
     * 提示词配置内部类，用于管理不同类型的提示词<br>
     * 目前包含代码查重相关的提示词配置
     */
    public static class Prompts {
        /**
         * 代码查重相关的提示词配置
         */
        private Plagiarism plagiarism = new Plagiarism();
        
        /**
         * 获取代码查重相关的提示词配置
         * @return Plagiarism对象，包含代码查重提示词
         */
        public Plagiarism getPlagiarism() {
            return plagiarism;
        }
        
        /**
         * 设置代码查重相关的提示词配置
         * @param plagiarism 代码查重提示词配置
         */
        public void setPlagiarism(Plagiarism plagiarism) {
            this.plagiarism = plagiarism;
        }
    }
    
    /**
     * 代码查重提示词配置内部类<br>
     * 管理代码查重分析过程中使用的AI助手提示词
     */
    public static class Plagiarism {
        /**
         * 代码查重助手提示词，用于指导AI如何分析代码相似度和提供建议
         */
        private String assistant;
        
        /**
         * 获取代码查重助手提示词
         * @return 提示词字符串，包含AI助手的角色和任务描述
         */
        public String getAssistant() {
            return assistant;
        }
        
        /**
         * 设置代码查重助手提示词
         * @param assistant 提示词字符串
         */
        public void setAssistant(String assistant) {
            this.assistant = assistant;
        }
    }
}