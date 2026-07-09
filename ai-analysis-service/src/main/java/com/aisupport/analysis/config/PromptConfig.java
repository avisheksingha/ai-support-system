package com.aisupport.analysis.config;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class PromptConfig {

    @Bean
    PromptTemplate ticketAnalysisPromptTemplate(
            @Value("classpath:prompts/ticket-analysis-prompt.st") Resource resource) {
        return new PromptTemplate(resource);
    }
}