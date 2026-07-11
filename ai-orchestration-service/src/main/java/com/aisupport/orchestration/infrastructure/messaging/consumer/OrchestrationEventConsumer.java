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

    @KafkaListener(topics = "ticket-analyzed", groupId = "orchestration-service-group")
    public void onTicketAnalyzed(String payload) {
        log.info("Received ticket-analyzed event");
        String workflowId = triggerRegistry.getWorkflowIdForTrigger("ticket-analyzed");
        
        if (workflowId != null) {
             log.info("Triggering workflow: {}", workflowId);
             
             // Setup basic context based on payload
             WorkflowContext context = WorkflowContext.builder()
                     .executionId(UUID.randomUUID().toString())
                     .correlationId(UUID.randomUUID().toString())
                     .build();
                     
             workflowEngine.execute(workflowId, context);
        } else {
             log.warn("No workflow registered for trigger: ticket-analyzed");
        }
    }
}
