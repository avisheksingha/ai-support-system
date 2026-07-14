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
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Import(TestAiConfiguration.class)
@RequiredArgsConstructor
class WorkflowPersistenceIT extends AbstractIntegrationTest {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final WorkflowExecutionRepository workflowExecutionRepository;

    private final WorkflowCheckpointRepository workflowCheckpointRepository;

    private final AiExecutionRecordRepository aiExecutionRecordRepository;

    @Test
    void testFullPersistenceLifecycle() {
        // Given
        String ticketNumber = UUID.randomUUID().toString();
        Long ticketId = 100L;
        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(ticketId); event.setTicketNumber(ticketNumber);
        event.setSubject("Persistence Test");
        event.setMessage("Verify all entities are saved");

        // When
        kafkaTemplate.send("ticket-created", ticketNumber, event);

        // Then - Wait for workflow completion and verify ALL persistence layers
        String expectedCorrelationId = "ticket-" + ticketId;
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            WorkflowExecutionEntity execution = workflowExecutionRepository.findAll().stream()
                    .filter(e -> expectedCorrelationId.equals(e.getCorrelationId()))
                    .findFirst()
                    .orElse(null);
            
            assertThat(execution).isNotNull();
            assertThat(execution.getState()).isEqualTo(WorkflowState.COMPLETED);

            // Verify checkpoints were saved
            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository.findAll().stream()
                    .filter(c -> c.getExecution().getId().equals(execution.getId()))
                    .toList();
            // Depending on how many states we have, we should see checkpoints
            assertThat(checkpoints).isNotEmpty();

            // Verify AI Audit record
            List<AiExecutionRecordEntity> auditRecords = aiExecutionRecordRepository.findAll().stream()
                    .filter(a -> a.getCorrelationId().equals(execution.getCorrelationId()))
                    .toList();
            assertThat(auditRecords).isNotEmpty();
            assertThat(auditRecords.get(0).getOutcome()).isEqualTo("SUCCESS");
        });
    }
}
















