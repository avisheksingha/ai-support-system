package com.aisupport.routing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.routing.entity.RoutingRule;
import com.aisupport.routing.entity.RuleExecutionHistory;
import com.aisupport.routing.repository.RoutingRuleRepository;
import com.aisupport.routing.repository.RuleExecutionHistoryRepository;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationServiceTest {

    @Mock
    private RoutingRuleRepository routingRuleRepository;
    @Mock
    private RuleExecutionHistoryRepository historyRepository;

    private RuleEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new RuleEvaluationService(routingRuleRepository, historyRepository);
    }

    @Test
    void evaluate_shouldReturnFirstMatchingRuleAndSaveHistoryPerRule() {
        RoutingRule first = RoutingRule.builder()
                .id(1L)
                .ruleName("NoMatch")
                .intentPattern("REFUND")
                .assignToTeam("refund-team")
                .build();
        RoutingRule second = RoutingRule.builder()
                .id(2L)
                .ruleName("PaymentMatch")
                .intentPattern("PAYMENT_ISSUE")
                .sentimentPattern("NEGATIVE")
                .urgencyPattern("*")
                .assignToTeam("billing-team")
                .build();
        when(routingRuleRepository.findActiveRulesOrderedByPriority()).thenReturn(List.of(first, second));

        TicketAnalyzedEvent event = TicketAnalyzedEvent.builder()
                .ticketId(100L)
                .intent("PAYMENT_ISSUE")
                .sentiment("NEGATIVE")
                .urgency("HIGH")
                .keywords(List.of("payment"))
                .build();

        RoutingRule matched = service.evaluate(event);

        assertThat(matched).isNotNull();
        assertThat(matched.getId()).isEqualTo(2L);
        verify(historyRepository, times(2)).save(org.mockito.ArgumentMatchers.any(RuleExecutionHistory.class));
    }

    @Test
    void evaluate_whenNoRuleMatches_shouldReturnNull() {
        RoutingRule onlyRule = RoutingRule.builder()
                .id(11L)
                .ruleName("Mismatch")
                .intentPattern("TECHNICAL_ISSUE")
                .assignToTeam("tech-team")
                .build();
        when(routingRuleRepository.findActiveRulesOrderedByPriority()).thenReturn(List.of(onlyRule));

        TicketAnalyzedEvent event = TicketAnalyzedEvent.builder()
                .ticketId(200L)
                .intent("PAYMENT_ISSUE")
                .sentiment("NEGATIVE")
                .urgency("HIGH")
                .build();

        RoutingRule matched = service.evaluate(event);

        assertThat(matched).isNull();

        ArgumentCaptor<RuleExecutionHistory> captor = ArgumentCaptor.forClass(RuleExecutionHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getRuleId()).isEqualTo(11L);
        assertThat(captor.getValue().getMatched()).isFalse();
    }
}
