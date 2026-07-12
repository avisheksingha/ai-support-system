package com.aisupport.orchestration.infrastructure.messaging.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.workflow.WorkflowEngine;
import com.aisupport.orchestration.application.workflow.WorkflowTriggerRegistry;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestrationEventConsumer {

    private final WorkflowTriggerRegistry triggerRegistry;
    private final WorkflowEngine workflowEngine;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @KafkaListener(topics = "ticket-created", groupId = "orchestration-service-group")
    public void onTicketCreated(String payload) {
        log.info("Received ticket-created event");
        String workflowId = triggerRegistry.getWorkflowIdForTrigger("ticket-created");
        
        if (workflowId != null) {
             log.info("Triggering workflow: {}", workflowId);
             
             try {
                 com.aisupport.common.event.TicketCreatedEvent event = 
                         objectMapper.readValue(payload, com.aisupport.common.event.TicketCreatedEvent.class);
                         
                 WorkflowContext context = WorkflowContext.builder()
                         .executionId(UUID.randomUUID().toString())
                         .correlationId(UUID.randomUUID().toString())
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
