package com.aisupport.routing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.event.EventType;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.routing.entity.RoutingRule;
import com.aisupport.routing.outbox.OutboxEventService;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private RuleEvaluationService ruleEvaluationService;
    @Mock
    private OutboxEventService outboxEventService;

    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new RoutingService(ruleEvaluationService, outboxEventService);
    }

    @Test
    void route_withMatchedRule_shouldPublishRuleDrivenEvent() {
        com.aisupport.common.event.AnalysisResult analysis = new com.aisupport.common.event.AnalysisResult(
                "PAYMENT_ISSUE", "NEGATIVE", "HIGH", 0.9, java.util.Collections.emptyList(), "Billing");
        TicketAnalyzedEvent analyzed = TicketAnalyzedEvent.builder()
                .ticketId(1L)
                .analysis(analysis)
                .build();

        RoutingRule rule = RoutingRule.builder()
                .assignToTeam("billing-team")
                .priorityOverride(TicketPriority.CRITICAL)
                .slaHours(4)
                .build();
        when(ruleEvaluationService.evaluate(analyzed)).thenReturn(rule);

        routingService.route(analyzed);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventService).publishEvent(
                anyString(),
                anyString(),
                ArgumentMatchers.any(EventType.class),
                payloadCaptor.capture()
        );

        TicketRoutedEvent published = (TicketRoutedEvent) payloadCaptor.getValue();
        assertThat(published.getAssignToTeam()).isEqualTo("billing-team");
        assertThat(published.getPriority()).isEqualTo(TicketPriority.CRITICAL);
        assertThat(published.getSlaHours()).isEqualTo(4);
        assertThat(published.getIntent()).isEqualTo("PAYMENT_ISSUE");
    }

    @Test
    void route_withoutMatchedRule_shouldUseFallbackValues() {
        com.aisupport.common.event.AnalysisResult analysis = new com.aisupport.common.event.AnalysisResult(
                "GENERAL", "NEUTRAL", "LOW", 0.0, java.util.Collections.emptyList(), "General");
        TicketAnalyzedEvent analyzed = TicketAnalyzedEvent.builder()
                .ticketId(2L)
                .analysis(analysis)
                .build();
        when(ruleEvaluationService.evaluate(analyzed)).thenReturn(null);

        routingService.route(analyzed);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventService).publishEvent(
                anyString(),
                anyString(),
                ArgumentMatchers.any(EventType.class),
                payloadCaptor.capture()
        );

        TicketRoutedEvent published = (TicketRoutedEvent) payloadCaptor.getValue();
        assertThat(published.getAssignToTeam()).isEqualTo("general-support");
        assertThat(published.getPriority()).isEqualTo(TicketPriority.MEDIUM);
        assertThat(published.getSlaHours()).isEqualTo(24);
    }
}
