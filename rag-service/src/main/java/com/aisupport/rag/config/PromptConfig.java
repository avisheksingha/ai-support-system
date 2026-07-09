package com.aisupport.rag.config;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class PromptConfig {

    @Bean
    PromptTemplate ragSystemPromptTemplate(
            @Value("classpath:prompts/rag-system-prompt.st") Resource resource) {
        return new PromptTemplate(resource);
    }
}