package com.aisupport.analysis.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    ChatClient geminiChatClient(GoogleGenAiChatModel geminiChatModel) {
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
	 * Creates the Gemini ChatProvider when chat.provider=gemini
	 * (or when the property is not specified).
	 */
    @Bean
    @ConditionalOnProperty(name = "chat.provider", havingValue = "gemini", matchIfMissing = true)
    ChatProvider geminiChatProvider(ChatClient geminiChatClient) {
        return new GeminiChatProvider(geminiChatClient);
    }

    /**
     * Creates the OpenAI ChatProvider when chat.provider=openai.
     */
    @Bean
    @ConditionalOnProperty(name = "chat.provider", havingValue = "openai")
    ChatProvider openAiChatProvider(ChatClient openAiChatClient) {
        return new OpenAiChatProvider(openAiChatClient);
    }
}
