package com.aisupport.orchestration.application.workflow.listener;

import java.time.Instant;
import java.util.UUID;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.enums.TicketStatus;
import com.aisupport.common.enums.WorkflowOutcome;
import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.EventMetadata;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.RoutingDecision;
import com.aisupport.common.event.TicketOrchestratedEvent;
import com.aisupport.orchestration.application.workflow.WorkflowExecutionListener;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowExecutionResult;
import com.aisupport.orchestration.infrastructure.persistence.entity.OutboxEventEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(40)
@RequiredArgsConstructor
public class OutboxWorkflowExecutionListener implements WorkflowExecutionListener {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void afterWorkflow(WorkflowContext context) {
        if (context.getTicketId() == null) {
            log.debug("No ticket ID in context, skipping Outbox event publishing for execution {}", context.getExecutionId());
            return;
        }

        try {
            AnalysisResult analysis = context.getResource(AnalysisResult.class);
            RoutingDecision routing = context.getResource(RoutingDecision.class);
            KnowledgeContext knowledge = context.getResource(KnowledgeContext.class);

            WorkflowExecutionResult result = WorkflowExecutionResult.builder()
                .analysis(analysis)
                .routing(routing)
                .knowledge(knowledge)
                .build();

            EventMetadata metadata = new EventMetadata(
                UUID.randomUUID().toString(),
                1,
                context.getCorrelationId(),
                context.getExecutionId(),
                "1.0",
                "1.0",
                "default",
                "1.0",
                100L, // Placeholder for processing duration
                WorkflowOutcome.SUCCESS,
                Instant.now()
            );

            TicketOrchestratedEvent event = new TicketOrchestratedEvent(
                    context.getTicketId(),
                    TicketStatus.ANALYZED,
                    result.getAnalysis(),
                    result.getRouting(),
                    result.getKnowledge(),
                    metadata
            );

            OutboxEventEntity entity = OutboxEventEntity.builder()
                    .id(metadata.eventId())
                    .aggregateType("Ticket")
                    .aggregateId(String.valueOf(context.getTicketId()))
                    .correlationId(context.getCorrelationId())
                    .eventType("TicketOrchestratedEvent")
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(metadata.generatedAt())
                    .build();

            outboxEventRepository.save(entity);
            log.info("Saved outbox event {} for ticket {}", "TicketOrchestratedEvent", context.getTicketId());
        } catch (Exception e) {
            log.error("Failed to save outbox event for execution {}", context.getExecutionId(), e);
        }
    }
}
