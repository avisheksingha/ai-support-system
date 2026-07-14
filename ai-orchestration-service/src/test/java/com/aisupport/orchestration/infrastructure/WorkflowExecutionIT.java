package com.aisupport.orchestration.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
class WorkflowExecutionIT extends AbstractIntegrationTest {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final WorkflowExecutionRepository workflowExecutionRepository;

    @Test
    void testWorkflowExecutionCompletesWithMockTools() {
        // Given
        String ticketNumber = UUID.randomUUID().toString();
        Long ticketId = 100L;
        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(ticketId); event.setTicketNumber(ticketNumber);
        event.setSubject("Test Workflow Execution");
        event.setMessage("Verify agent and tool calls");

        // When
        kafkaTemplate.send("ticket-created", ticketNumber, event);

        // Then - Wait for the workflow to reach COMPLETED state
        String expectedCorrelationId = "ticket-" + ticketId;
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            WorkflowExecutionEntity execution = workflowExecutionRepository.findAll().stream()
                    .filter(e -> expectedCorrelationId.equals(e.getCorrelationId()))
                    .findFirst()
                    .orElse(null);
            
            assertThat(execution).isNotNull();
            assertThat(execution.getState()).isEqualTo(WorkflowState.COMPLETED);
            
            // Context, Prompt, Agent, Tools should have executed
            // Since we use mock mode for tools and mock ChatClient, we verify success by the end state
            // Optional: verify that mock tools were recorded if the mock agent called them.
            // Our TestAiConfiguration currently just returns a final recommendation immediately.
            // If we want it to call tools, we could enhance TestAiConfiguration to return tool calls first.
        });
    }
}
















