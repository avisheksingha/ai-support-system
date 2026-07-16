package com.aisupport.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import com.aisupport.analysis.dto.request.WritingContext;
import com.aisupport.analysis.dto.request.WritingImproveRequest;
import com.aisupport.analysis.dto.response.WritingImproveResponse;

@ExtendWith(MockitoExtension.class)
class WritingAssistantServiceTest {

    private ChatClient chatClient;

    @Mock
    private PromptTemplate writingSupportTicketPromptTemplate;

    @Mock
    private PromptTemplate writingAgentReplyPromptTemplate;

    private WritingAssistantService service;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        service = new WritingAssistantService(
                chatClient,
                writingSupportTicketPromptTemplate,
                writingAgentReplyPromptTemplate
        );
    }

    @Test
    void improve_shouldReturnAIResponse() {
        when(writingSupportTicketPromptTemplate.getTemplate()).thenReturn("System Prompt");

        WritingImproveResponse expectedResponse = new WritingImproveResponse(
                "Help",
                "I need help.",
                List.of("Grammar fix"),
                true,
                "Model"
        );

        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .entity(WritingImproveResponse.class))
                .thenReturn(expectedResponse);

        WritingImproveRequest request = new WritingImproveRequest(
                WritingContext.SUPPORT_TICKET,
                "Help",
                "I needs help",
                "en"
        );

        WritingImproveResponse actualResponse = service.improve(request);

        assertThat(actualResponse.improvedContent()).isEqualTo("I need help.");
        assertThat(actualResponse.improved()).isTrue();
    }

    @Test
    void improve_whenProviderFails_shouldReturnFallback() {
        when(writingSupportTicketPromptTemplate.getTemplate()).thenReturn("System Prompt");

        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .entity(WritingImproveResponse.class))
                .thenThrow(new RuntimeException("API error"));

        WritingImproveRequest request = new WritingImproveRequest(
                WritingContext.SUPPORT_TICKET,
                "Help",
                "I needs help",
                "en"
        );

        WritingImproveResponse actualResponse = service.improve(request);

        assertThat(actualResponse.improvedContent()).isEqualTo("I needs help"); // Original text
        assertThat(actualResponse.improved()).isFalse();
        assertThat(actualResponse.changes()).contains("AI service unavailable. Original text retained.");
    }
}
