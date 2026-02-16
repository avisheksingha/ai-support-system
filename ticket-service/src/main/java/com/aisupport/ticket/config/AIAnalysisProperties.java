package com.aisupport.ticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "api.services.ai-analysis")
public class AIAnalysisProperties {
    private String url;
}
