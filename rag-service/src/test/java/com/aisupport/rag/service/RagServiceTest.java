package com.aisupport.rag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.test.util.ReflectionTestUtils;

import com.aisupport.common.event.EventType;
import com.aisupport.common.event.TicketRagResponseEvent;
import com.aisupport.rag.entity.RagResponse;
import com.aisupport.rag.exception.RagGenerationException;
import com.aisupport.rag.outbox.OutboxEventService;
import com.aisupport.rag.repository.KnowledgeArticleRepository;
import com.aisupport.rag.repository.RagResponseRepository;
import com.aisupport.rag.service.retrieval.MetadataAwareRetrievalService;
import com.aisupport.rag.service.retrieval.RagPromptFactory;
import com.aisupport.rag.service.retrieval.RetrievalResult;
import com.aisupport.rag.service.serialization.SourceMetadataSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    private ChatClient chatClient;

    @Mock
    private MetadataAwareRetrievalService retrievalService;
    @Mock
    private RagResponseRepository ragResponseRepository;
    @Mock
    private KnowledgeArticleRepository knowledgeArticleRepository;
    @Mock
    private OutboxEventService outboxEventService;

    private final PromptTemplate ragSystemPromptTemplate =
            new PromptTemplate("You are a support assistant. No answer: {noKnowledgeFound}");

    private RagService ragService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);
        RagPromptFactory promptFactory = new RagPromptFactory(ragSystemPromptTemplate);
        SourceMetadataSerializer serializer = new SourceMetadataSerializer(new ObjectMapper());

        ragService = new RagService(
                chatClient,
                retrievalService,
                ragResponseRepository,
                knowledgeArticleRepository,
                outboxEventService,
                promptFactory,
                serializer
        );
        ReflectionTestUtils.setField(ragService, "chatModel", "gemini-2.5-flash");
    }

    @Test
    void generateResponse_shouldPersistAndPublishEvent() {
        Document doc = Document.builder()
                .id("101")
                .text("Refund policy details")
                .metadata(Map.of("articleId", 101L, "title", "Refund Policy", "category", "BILLING"))
                .build();
        RetrievalResult nonEmptyResult = new RetrievalResult(List.of(doc), "Refund policy details", 1, "Refund Policy");
        when(retrievalService.retrieveAndRank(anyString())).thenReturn(nonEmptyResult);

        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .chatResponse())
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("Suggested response")))));

        String result = ragService.generateResponse(7L, "refund failed");

        assertThat(result).isEqualTo("Suggested response");

        ArgumentCaptor<RagResponse> responseCaptor = ArgumentCaptor.forClass(RagResponse.class);
        verify(ragResponseRepository).save(responseCaptor.capture());
        assertThat(responseCaptor.getValue().getTicketId()).isEqualTo(7L);
        assertThat(responseCaptor.getValue().getModel()).isEqualTo("gemini-2.5-flash");
        assertThat(responseCaptor.getValue().getKnowledgeFound()).isTrue();
        assertThat(responseCaptor.getValue().getRetrievedDocumentCount()).isEqualTo(1);

        verify(knowledgeArticleRepository).incrementAccessCountByIds(List.of(101L));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventService).publishEvent(anyString(), anyString(), ArgumentMatchers.any(EventType.class), eventCaptor.capture());
        TicketRagResponseEvent event = (TicketRagResponseEvent) eventCaptor.getValue();
        assertThat(event.getTicketId()).isEqualTo(7L);
        assertThat(event.getModel()).isEqualTo("gemini-2.5-flash");
        assertThat(event.getResponse()).isEqualTo("Suggested response");
    }

    @Test
    void generateResponse_whenNoKnowledgeFound_shouldPersistKnowledgeFoundFalse() {
        when(retrievalService.retrieveAndRank(anyString())).thenReturn(RetrievalResult.empty());

        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .chatResponse())
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("No relevant knowledge article found.\n")))));

        String result = ragService.generateResponse(9L, "unknown issue");

        assertThat(result).isEqualTo("No relevant knowledge article found.\n");

        ArgumentCaptor<RagResponse> responseCaptor = ArgumentCaptor.forClass(RagResponse.class);
        verify(ragResponseRepository).save(responseCaptor.capture());
        assertThat(responseCaptor.getValue().getTicketId()).isEqualTo(9L);
        assertThat(responseCaptor.getValue().getKnowledgeFound()).isFalse();
        assertThat(responseCaptor.getValue().getRetrievedDocumentCount()).isEqualTo(0);

        verify(knowledgeArticleRepository, never()).incrementAccessCountByIds(any());
        verify(knowledgeArticleRepository, never()).incrementAccessCountByTitles(any());
    }

    @Test
    void generateResponse_whenChatFails_shouldThrowDomainException() {
        when(retrievalService.retrieveAndRank(anyString())).thenReturn(RetrievalResult.empty());

        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .chatResponse())
                .thenThrow(new RuntimeException("provider down"));

        assertThatThrownBy(() -> ragService.generateResponse(8L, "query"))
                .isInstanceOf(RagGenerationException.class)
                .hasMessageContaining("ticketId: 8");

        verify(ragResponseRepository, never()).save(any());
        verify(outboxEventService, never()).publishEvent(anyString(), anyString(), ArgumentMatchers.any(EventType.class), any());
    }
}
