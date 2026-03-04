package com.aisupport.analysis.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.aisupport.analysis.service.AiProvider;
import com.aisupport.analysis.service.GeminiService;
import com.aisupport.analysis.service.OpenAiService;

/**
 * Wires up the active {@link AiProvider} based on the {@code ai.provider} property.
 *
 * <pre>
 * ai.provider=gemini   → GeminiService  (default)
 * ai.provider=openai   → OpenAiService
 * </pre>
 *
 * Both underlying {@link ChatClient} beans are always created so that their
 * respective auto-configurations are properly initialised. Only the chosen
 * provider is exposed as the {@code @Primary} {@link AiProvider} bean.
 */
@Configuration
public class AIConfig {

    // -------------------------------------------------------------------------
    // ChatClient beans (one per provider)
    // -------------------------------------------------------------------------

    @Bean    
    ChatClient geminiChatClient(VertexAiGeminiChatModel geminiChatModel) {
        return ChatClient.create(geminiChatModel);
    }

    @Bean    
    ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel);
    }

    // -------------------------------------------------------------------------
    // AiProvider strategy selection — driven by ai.provider property
    // -------------------------------------------------------------------------

    /**
     * Activates GeminiService as the primary AiProvider when ai.provider=gemini (default).
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.provider", havingValue = "gemini", matchIfMissing = true)
    AiProvider geminiProvider(@Qualifier("geminiChatClient") ChatClient chatClient) {
        return new GeminiService(chatClient);
    }

    /**
     * Activates OpenAiService as the primary AiProvider when ai.provider=openai.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
    AiProvider openAiProvider(@Qualifier("openAiChatClient") ChatClient chatClient) {
        return new OpenAiService(chatClient);
    }
}
