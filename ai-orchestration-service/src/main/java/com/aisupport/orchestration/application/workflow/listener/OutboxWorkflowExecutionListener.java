package com.aisupport.orchestration.application.workflow.listener;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.application.workflow.WorkflowExecutionListener;
import com.aisupport.orchestration.domain.event.outbox.TicketOrchestratedEvent;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
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
            TicketOrchestratedEvent event = TicketOrchestratedEvent.builder()
                    .ticketId(context.getTicketId())
                    .status("COMPLETED")
                    .build();

            OutboxEventEntity entity = OutboxEventEntity.builder()
                    .id(event.getEventId())
                    .aggregateType("Ticket")
                    .aggregateId(String.valueOf(context.getTicketId()))
                    .eventType(event.getEventType())
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(event.getCreatedAt())
                    .build();

            outboxEventRepository.save(entity);
            log.info("Saved outbox event {} for ticket {}", event.getEventType(), context.getTicketId());
        } catch (Exception e) {
            log.error("Failed to save outbox event for execution {}", context.getExecutionId(), e);
        }
    }
}
