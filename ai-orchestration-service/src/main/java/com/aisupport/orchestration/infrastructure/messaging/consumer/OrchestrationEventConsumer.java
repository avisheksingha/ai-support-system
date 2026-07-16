package com.aisupport.orchestration.infrastructure.messaging.consumer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.aisupport.common.constants.Correlation;
import com.aisupport.common.constants.HttpHeaders;
import com.aisupport.common.constants.KafkaGroups;
import com.aisupport.common.constants.KafkaTopics;
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
    
    @KafkaListener(
    		topics = KafkaTopics.TICKET_CREATED,
    		groupId = KafkaGroups.AI_ORCHESTRATION)
    public void onTicketCreated(ConsumerRecord<String, TicketCreatedEvent> consumerRecord) {
    	
    	TicketCreatedEvent event = consumerRecord.value();
    	
    	String correlationId = extractCorrelationId(consumerRecord, event);

        MDC.put(Correlation.MDC_KEY, correlationId);

        try {
        	log.info("Consumed ticket-created event: ticketId={}", event.getTicketId());

            String workflowId =
                    triggerRegistry.getWorkflowIdForTrigger(KafkaTopics.TICKET_CREATED);

            if (workflowId == null) {
                log.warn("No workflow registered for trigger {}", KafkaTopics.TICKET_CREATED);
                return;
            }

            // Idempotency
            if (workflowExecutionRepository.findByCorrelationId(correlationId).isPresent()) {
                log.info("Workflow already executed for correlationId={}", correlationId);
                return;
            }

            WorkflowContext context = WorkflowContext.builder()
                    .executionId(UUID.randomUUID().toString())
                    .correlationId(correlationId)
                    .ticketId(event.getTicketId())
                    .build();

            context.putAttribute("subject", event.getSubject());
            context.putAttribute("message", event.getMessage());

            log.info("Triggering workflow {} for ticket {}", workflowId, event.getTicketId());

            workflowEngine.execute(workflowId, context);
            
        } catch (Exception e) {
            log.error("Failed to process ticket-created event key={}", consumerRecord.key(), e);
            throw new TicketEventProcessingException("Error processing ticket-created event", e);
        } finally {
            MDC.remove(Correlation.MDC_KEY);
        }
    }

    private String extractCorrelationId(ConsumerRecord<?, ?> consumerRecord,
            TicketCreatedEvent event) {

        Header correlationHeader =
                consumerRecord.headers().lastHeader(HttpHeaders.CORRELATION_ID);

        if (correlationHeader != null) {
            return new String(
                    correlationHeader.value(),
                    StandardCharsets.UTF_8);
        }

        // Fallback for local/manual publishers
        log.warn("CorrelationId header missing. Falling back to ticket-based correlationId.");

        return "ticket-" + event.getTicketId();
    }
}
