package org.codeDuplicateChecking.Agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DashScope配置类
 * 使用ConfigurationProperties绑定dashscope配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "dashscope")
@Data
public class DashScopeConfig {
    
    private Api api = new Api();
    private String model;
    private boolean streamEnabled;
    private double temperature;
    private double topP;
    
    @Data
    public static class Api {
        private String key;
    }
}