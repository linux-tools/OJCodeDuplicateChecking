package org.codeDuplicateChecking.Agent.config;

import com.alibaba.dashscope.aigc.generation.Generation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QwenConfig {

    private final DashScopeConfig dashScopeConfig;

    public QwenConfig(DashScopeConfig dashScopeConfig) {
        this.dashScopeConfig = dashScopeConfig;
    }

    @Bean
    public Generation generation() {
        return new Generation();
    }

    @Bean
    public QwenProperties qwenProperties() {
        return new QwenProperties(dashScopeConfig.getApi().getKey(), dashScopeConfig.getModel());
    }

    public static class QwenProperties {
        private final String apiKey;
        private final String model;

        public QwenProperties(String apiKey, String model) {
            this.apiKey = apiKey;
            this.model = model;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getModel() {
            return model;
        }
    }
}