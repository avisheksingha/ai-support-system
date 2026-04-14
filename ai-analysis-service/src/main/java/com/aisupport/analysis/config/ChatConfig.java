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
	 * Creates Gemini ChatClient when Gemini is the active provider.
	 */
    @Bean
    @ConditionalOnProperty(name = "chat.provider", havingValue = "gemini", matchIfMissing = true)
    ChatClient geminiChatClient(VertexAiGeminiChatModel geminiChatModel) {
        return ChatClient.create(geminiChatModel);
    }

    /**
	 * Creates OpenAI ChatClient only when OpenAI is the active provider.
	 */
    @Bean
    @ConditionalOnProperty(name = "chat.provider", havingValue = "openai")
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
