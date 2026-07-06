package com.aisupport.analysis.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aisupport.analysis.chat.ChatProvider;
import com.aisupport.analysis.chat.GoogleGenAiChatProvider;
import com.aisupport.analysis.chat.OpenAiChatProvider;

/**
 * ChatConfig centralizes the configuration of AI providers and their ChatClient beans.
 * It uses Spring's @ConditionalOnProperty to switch between Google GenAI and OpenAI implementations
 * based on the "chat.provider" property, allowing for flexible provider selection without code changes.
 */
@Configuration
public class ChatConfig {

	/**
	 * Creates Google GenAI ChatClient when Google GenAI is the active provider.
	 */
    @Bean
    @ConditionalOnProperty(name = "chat.provider", havingValue = "google-genai", matchIfMissing = true)
    ChatClient googleGenAiChatClient(GoogleGenAiChatModel googleGenAiChatModel) {
        return ChatClient.create(googleGenAiChatModel);
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
	 * Creates the Google GenAI ChatProvider when chat.provider=google-genai
	 * (or when the property is not specified).
	 */
    @Bean
    @ConditionalOnProperty(name = "chat.provider", havingValue = "google-genai", matchIfMissing = true)
    ChatProvider googleGenAiChatProvider(ChatClient googleGenAiChatClient) {
        return new GoogleGenAiChatProvider(googleGenAiChatClient);
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
