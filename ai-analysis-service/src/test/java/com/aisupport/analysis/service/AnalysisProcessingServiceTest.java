package com.aisupport.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aisupport.analysis.chat.ChatProvider;
import com.aisupport.analysis.dto.ParsedAnalysis;
import com.aisupport.analysis.entity.AnalysisResult;
import com.aisupport.analysis.outbox.OutboxEventService;
import com.aisupport.analysis.repository.AnalysisResultRepository;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AnalysisProcessingServiceTest {

    @Mock
    private ChatProvider chatProvider;
    @Mock
    private AnalysisResultRepository repository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private OutboxEventService outboxEventService;

    private AnalysisProcessingService service;

    @BeforeEach
    void setUp() {
        service = new AnalysisProcessingService(chatProvider, repository, objectMapper, outboxEventService);
    }

    @Test
    void processTicket_whenAlreadyAnalyzed_shouldSkipProcessing() {
        TicketCreatedEvent event = TicketCreatedEvent.builder()
                .ticketId(10L)
                .subject("subject")
                .message("message")
                .build();
        when(repository.existsByTicketId(10L)).thenReturn(true);

        service.processTicket(event);

        verify(chatProvider, never()).analyzeTicket(anyString(), anyString());
        verify(repository, never()).save(any(AnalysisResult.class));
        verify(outboxEventService, never()).publishEvent(anyString(), anyString(), anyString(), any());
    }

    @Test
    void processTicket_shouldPersistNormalizedResultAndPublishEvent() throws Exception {
        TicketCreatedEvent event = TicketCreatedEvent.builder()
                .ticketId(42L)
                .subject("Payment issue")
                .message("Payment failed twice for same order")
                .build();

        ParsedAnalysis parsed = ParsedAnalysis.builder()
                .intent("payment error")
                .sentiment(null)
                .urgency(null)
                .confidenceScore(null)
                .keywords(List.of("payment", "error"))
                .suggestedCategory("Billing")
                .build();

        when(repository.existsByTicketId(42L)).thenReturn(false);
        when(chatProvider.analyzeTicket(event.getSubject(), event.getMessage())).thenReturn(parsed);
        when(objectMapper.writeValueAsString(parsed)).thenReturn("{\"ok\":true}");

        service.processTicket(event);

        ArgumentCaptor<AnalysisResult> savedCaptor = ArgumentCaptor.forClass(AnalysisResult.class);
        verify(repository).save(savedCaptor.capture());
        AnalysisResult saved = savedCaptor.getValue();
        assertThat(saved.getTicketId()).isEqualTo(42L);
        assertThat(saved.getIntent()).isEqualTo("PAYMENT_ISSUE");
        assertThat(saved.getSentiment()).isEqualTo("NEUTRAL");
        assertThat(saved.getUrgency()).isEqualTo("LOW");
        assertThat(saved.getConfidenceScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getRawResponse()).isEqualTo("{\"ok\":true}");

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventService).publishEvent(
                anyString(),
                anyString(),
                anyString(),
                eventCaptor.capture()
        );
        TicketAnalyzedEvent published = (TicketAnalyzedEvent) eventCaptor.getValue();
        assertThat(published.getTicketId()).isEqualTo(42L);
        assertThat(published.getIntent()).isEqualTo("PAYMENT_ISSUE");
        assertThat(published.getSentiment()).isEqualTo("NEUTRAL");
        assertThat(published.getUrgency()).isEqualTo("LOW");
    }
}
