package com.aisupport.orchestration.infrastructure;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestAiConfiguration {

    @Bean
    @Primary
    ChatModel mockChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                // Return a mock successful response with tool call or final text based on prompt
                String responseText = "{\"recommendation\": \"Mock recommendation based on tools\"}";
                Generation generation = new Generation(new org.springframework.ai.chat.messages.AssistantMessage(responseText));
                return new ChatResponse(List.of(generation));
            }
        };
    }

    @Bean
    @Primary
    ChatClient mockChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}



