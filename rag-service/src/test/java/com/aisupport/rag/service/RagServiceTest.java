package com.aisupport.rag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.test.util.ReflectionTestUtils;

import com.aisupport.common.event.TicketRagResponseEvent;
import com.aisupport.rag.entity.RagResponse;
import com.aisupport.rag.exception.RagGenerationException;
import com.aisupport.rag.outbox.OutboxEventService;
import com.aisupport.rag.repository.RagResponseRepository;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    private ChatClient chatClient;

    @Mock
    private QuestionAnswerAdvisor questionAnswerAdvisor;
    @Mock
    private RagResponseRepository ragResponseRepository;
    @Mock
    private OutboxEventService outboxEventService;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);
        ragService = new RagService(chatClient, questionAnswerAdvisor, ragResponseRepository, outboxEventService);
        ReflectionTestUtils.setField(ragService, "chatModel", "gemini-2.5-flash");
    }

    @Test
    void generateResponse_shouldPersistAndPublishEvent() {
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .advisors(questionAnswerAdvisor)
                .call()
                .content())
                .thenReturn("Suggested response");

        String result = ragService.generateResponse(7L, "refund failed");

        assertThat(result).isEqualTo("Suggested response");

        ArgumentCaptor<RagResponse> responseCaptor = ArgumentCaptor.forClass(RagResponse.class);
        verify(ragResponseRepository).save(responseCaptor.capture());
        assertThat(responseCaptor.getValue().getTicketId()).isEqualTo(7L);
        assertThat(responseCaptor.getValue().getModel()).isEqualTo("gemini-2.5-flash");

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventService).publishEvent(anyString(), anyString(), anyString(), eventCaptor.capture());
        TicketRagResponseEvent event = (TicketRagResponseEvent) eventCaptor.getValue();
        assertThat(event.getTicketId()).isEqualTo(7L);
        assertThat(event.getModel()).isEqualTo("gemini-2.5-flash");
        assertThat(event.getResponse()).isEqualTo("Suggested response");
    }

    @Test
    void generateResponse_whenChatFails_shouldThrowDomainException() {
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .advisors(questionAnswerAdvisor)
                .call()
                .content())
                .thenThrow(new RuntimeException("provider down"));

        assertThatThrownBy(() -> ragService.generateResponse(8L, "query"))
                .isInstanceOf(RagGenerationException.class)
                .hasMessageContaining("ticketId: 8");

        verify(ragResponseRepository, never()).save(any());
        verify(outboxEventService, never()).publishEvent(anyString(), anyString(), anyString(), any());
    }
}
