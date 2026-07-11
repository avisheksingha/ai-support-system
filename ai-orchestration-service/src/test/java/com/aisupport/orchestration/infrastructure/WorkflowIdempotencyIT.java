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
class WorkflowIdempotencyIT extends AbstractIntegrationTest {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final WorkflowExecutionRepository workflowExecutionRepository;

    @Test
    void testDuplicateEventProcessedOnlyOnce() {
        // Given
        String ticketId = UUID.randomUUID().toString();
        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(100L); event.setTicketNumber(ticketId);
        event.setSubject("Idempotency Test");

        // When - Send it twice rapidly
        kafkaTemplate.send("ticket-analyzed", ticketId, event);
        kafkaTemplate.send("ticket-analyzed", ticketId, event);

        // Then
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            List<WorkflowExecutionEntity> executions = workflowExecutionRepository.findAll().stream()
                    .filter(e -> ticketId.equals(e.getCorrelationId()))
                    .toList();
            
            // Only one execution should be recorded for this ticketId despite two events
            assertThat(executions).hasSize(1);
            assertThat(executions.get(0).getState()).isEqualTo(WorkflowState.COMPLETED);
        });
        
        // Wait a bit more to ensure the second event didn't just lag
        await().pollDelay(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<WorkflowExecutionEntity> executionsAfterWait = workflowExecutionRepository.findAll().stream()
                        .filter(e -> ticketId.equals(e.getCorrelationId()))
                        .toList();
            assertThat(executionsAfterWait).hasSize(1);
        });
    }
}














