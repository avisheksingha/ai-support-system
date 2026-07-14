package com.aisupport.orchestration.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import com.aisupport.common.event.TicketCreatedEvent;
import com.aisupport.orchestration.domain.state.WorkflowState;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Import(TestAiConfiguration.class)
@RequiredArgsConstructor
class WorkflowInitializationIT extends AbstractIntegrationTest {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final WorkflowExecutionRepository workflowExecutionRepository;

    @Test
    void testWorkflowInitializationOnTicketCreatedEvent() {
        // Given
        String ticketNumber = UUID.randomUUID().toString();
        Long ticketId = 100L;
        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(ticketId); 
        event.setTicketNumber(ticketNumber);
        event.setSubject("Test Ticket for Initialization");
        event.setMessage("Need help with initialization");

        // When
        kafkaTemplate.send("ticket-created", ticketNumber, event);

        // Then - Wait for the orchestration service to pick it up and initialize the workflow
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // Check if the WorkflowExecutionEntity is created
            List<WorkflowExecutionEntity> executions = workflowExecutionRepository.findAll();
            assertThat(executions).isNotEmpty();
            
            String expectedCorrelationId = "ticket-" + ticketId;
            WorkflowExecutionEntity execution = executions.stream()
                    .filter(e -> expectedCorrelationId.equals(e.getCorrelationId()))
                    .findFirst()
                    .orElse(null);
            
            assertThat(execution).isNotNull();
            assertThat(execution.getTicketId()).isEqualTo(ticketId);
            assertThat(execution.getCorrelationId()).isEqualTo(expectedCorrelationId);
            assertThat(execution.getState()).isIn(
                    WorkflowState.CREATED,
                    WorkflowState.RUNNING,
                    WorkflowState.COMPLETED);
        });
    }





}
