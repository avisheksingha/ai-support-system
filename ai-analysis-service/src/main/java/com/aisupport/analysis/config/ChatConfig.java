package com.aisupport.analysis.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.aisupport.analysis.chat.ChatProvider;
import com.aisupport.analysis.chat.GeminiChatProvider;
import com.aisupport.analysis.chat.OpenAiChatProvider;

/**
 * ChatConfig centralizes the configuration of AI providers and their ChatClient beans.
 * It uses Spring's @ConditionalOnProperty to switch between Gemini and OpenAI implementations
 * based on the "chat.provider" property, allowing for flexible provider selection without code changes.
 */
@Configuration
public class ChatConfig {

	/**
	 * ChatClient beans for each provider. These are created regardless of which provider is active,
	 * but only the one marked with @Primary and matching the "chat.provider" property will be injected
	 * into the ChatProvider implementations.
	 */
    @Bean    
    ChatClient geminiChatClient(VertexAiGeminiChatModel geminiChatModel) {
        return ChatClient.create(geminiChatModel);
    }

    /**
	 * OpenAI ChatClient bean. This will be created but only used if chat.provider=openai.
	 */
    @Bean    
    ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel);
    }

    /**
	 * Activates GeminiChatProvider as the primary ChatProvider when chat.provider=gemini or if the property is missing.
	 */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "chat.provider", havingValue = "gemini", matchIfMissing = true)
    ChatProvider geminiChatProvider(@Qualifier("geminiChatClient") ChatClient chatClient) {
        return new GeminiChatProvider(chatClient);
    }

    /**
     * Activates OpenAiChatProvider as the primary ChatProvider when chat.provider=openai.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "chat.provider", havingValue = "openai")
    ChatProvider openAiChatProvider(@Qualifier("openAiChatClient") ChatClient chatClient) {
        return new OpenAiChatProvider(chatClient);
    }
}
