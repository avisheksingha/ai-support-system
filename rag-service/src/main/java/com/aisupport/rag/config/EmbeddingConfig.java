package com.aisupport.rag.config;

import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.aisupport.rag.embedding.EmbeddingProvider;
import com.aisupport.rag.embedding.GeminiEmbeddingProvider;

@Configuration
public class EmbeddingConfig {	
	
	/**
	 * Bean configuration for the active EmbeddingProvider implementation.
	 * The provider is selected based on the 'embedding.provider' property.
	 * Default is GeminiEmbeddingProvider if the property is not set.
	 */
	@Bean
    @Primary
    @ConditionalOnProperty(name = "embedding.provider", havingValue = "gemini", matchIfMissing = true)
    EmbeddingProvider geminiEmbeddingProvider(VertexAiTextEmbeddingModel model) {
        return new GeminiEmbeddingProvider(model);
    }
}