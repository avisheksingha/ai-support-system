package com.aisupport.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
/**
 * Configures the ChatClient bean used by RagService for response generation.
 * The ChatClient wraps the Google GenAI chat model and provides
 * a fluent API for building prompts, attaching advisors, and calling the LLM.
 */
@Configuration
public class ChatConfig {

    @Bean
    ChatClient ragChatClient(ChatModel model) {
        return ChatClient.create(model);
    }
}
