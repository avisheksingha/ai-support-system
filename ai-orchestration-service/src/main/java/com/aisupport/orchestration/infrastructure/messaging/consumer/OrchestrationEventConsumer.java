package com.aisupport.orchestration.infrastructure.messaging.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.aisupport.common.constant.Correlation;
import com.aisupport.common.constant.HttpHeaders;
import com.aisupport.common.constant.KafkaGroups;
import com.aisupport.common.constant.KafkaTopics;
import com.aisupport.common.event.TicketCreatedEvent;
import com.aisupport.common.exception.TicketEventProcessingException;
import com.aisupport.orchestration.application.workflow.WorkflowEngine;
import com.aisupport.orchestration.application.workflow.WorkflowTriggerRegistry;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestrationEventConsumer {

    private final WorkflowTriggerRegistry triggerRegistry;
    private final WorkflowEngine workflowEngine;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    
    @KafkaListener(topics = KafkaTopics.TICKET_CREATED, groupId = KafkaGroups.AI_ORCHESTRATION)
    public void onTicketCreated(ConsumerRecord<String, TicketCreatedEvent> consumerRecord) {
        Header correlationHeader = consumerRecord.headers().lastHeader(HttpHeaders.CORRELATION_ID);
        if (correlationHeader != null) {
            MDC.put(Correlation.MDC_KEY, new String(correlationHeader.value(), StandardCharsets.UTF_8));
        }

        try {
            TicketCreatedEvent event = consumerRecord.value();

            log.info("Received ticket-created event: ticketId={}", event.getTicketId());

            String workflowId = triggerRegistry.getWorkflowIdForTrigger(KafkaTopics.TICKET_CREATED);
            if (workflowId == null) {
                log.warn("No workflow registered for trigger: {}", KafkaTopics.TICKET_CREATED);
                return;
            }

            String workflowCorrelationId = "ticket-" + event.getTicketId();
            if (workflowExecutionRepository.findByCorrelationId(workflowCorrelationId).isPresent()) {
                log.info("Idempotency check: Workflow already executed for correlationId {}", workflowCorrelationId);
                return;
            }

            log.info("Triggering workflow: {}", workflowId);

            WorkflowContext context = WorkflowContext.builder()
                    .executionId(UUID.randomUUID().toString())
                    .correlationId(workflowCorrelationId)
                    .ticketId(event.getTicketId())
                    .build();

            context.putAttribute("subject", event.getSubject());
            context.putAttribute("message", event.getMessage());

            workflowEngine.execute(workflowId, context);
        } catch (Exception e) {
            log.error("Failed to process ticket-created event key={}", consumerRecord.key(), e);
            throw new TicketEventProcessingException("Error processing ticket-created event", e);
        } finally {
            MDC.remove(Correlation.MDC_KEY);
        }
    }
}
