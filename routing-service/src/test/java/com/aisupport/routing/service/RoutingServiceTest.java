package com.aisupport.routing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aisupport.common.enums.TicketPriority;
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
        TicketAnalyzedEvent analyzed = TicketAnalyzedEvent.builder()
                .ticketId(1L)
                .intent("PAYMENT_ISSUE")
                .sentiment("NEGATIVE")
                .urgency("HIGH")
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
                anyString(),
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
        TicketAnalyzedEvent analyzed = TicketAnalyzedEvent.builder()
                .ticketId(2L)
                .intent("GENERAL")
                .sentiment("NEUTRAL")
                .urgency("LOW")
                .build();
        when(ruleEvaluationService.evaluate(analyzed)).thenReturn(null);

        routingService.route(analyzed);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventService).publishEvent(
                anyString(),
                anyString(),
                anyString(),
                payloadCaptor.capture()
        );

        TicketRoutedEvent published = (TicketRoutedEvent) payloadCaptor.getValue();
        assertThat(published.getAssignToTeam()).isEqualTo("general-support");
        assertThat(published.getPriority()).isEqualTo(TicketPriority.MEDIUM);
        assertThat(published.getSlaHours()).isEqualTo(24);
    }
}
