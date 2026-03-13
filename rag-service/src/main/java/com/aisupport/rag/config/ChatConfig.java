package com.aisupport.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the ChatClient bean used by RagService for response generation.
 * The ChatClient wraps the Vertex AI Gemini chat model and provides
 * a fluent API for building prompts, attaching advisors, and calling the LLM.
 */
@Configuration
public class ChatConfig {

    @Bean
    ChatClient ragChatClient(VertexAiGeminiChatModel model) {
        return ChatClient.create(model);
    }
}
