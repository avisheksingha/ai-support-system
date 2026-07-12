package com.aisupport.orchestration.infrastructure.messaging.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.aisupport.common.event.TicketCreatedEvent;
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

    @KafkaListener(topics = "ticket-created", groupId = "orchestration-service-group")
    public void onTicketCreated(TicketCreatedEvent event) {
        log.info("Received ticket-created event");
        String workflowId = triggerRegistry.getWorkflowIdForTrigger("ticket-created");
        
        if (workflowId != null) {
             String correlationId = event.getTicketNumber() != null ? event.getTicketNumber() : UUID.randomUUID().toString();
             
             if (workflowExecutionRepository.findByCorrelationId(correlationId).isPresent()) {
                 log.info("Idempotency check: Workflow already executed for correlationId {}", correlationId);
                 return;
             }

             log.info("Triggering workflow: {}", workflowId);
             
             try {
                         
                 WorkflowContext context = WorkflowContext.builder()
                         .executionId(UUID.randomUUID().toString())
                         .correlationId(correlationId)
                         .ticketId(event.getTicketId())
                         .build();
                         
                 context.putAttribute("subject", event.getSubject());
                 context.putAttribute("message", event.getMessage());
                         
                 workflowEngine.execute(workflowId, context);
             } catch (Exception e) {
                 log.error("Failed to process ticket-created event", e);
             }
        } else {
             log.warn("No workflow registered for trigger: ticket-created");
        }
    }
}
