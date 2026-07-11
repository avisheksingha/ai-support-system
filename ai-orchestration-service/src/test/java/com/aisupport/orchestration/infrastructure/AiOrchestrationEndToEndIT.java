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
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Import(TestAiConfiguration.class)
@RequiredArgsConstructor
class AiOrchestrationEndToEndIT extends AbstractIntegrationTest {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final AiExecutionRecordRepository aiExecutionRecordRepository;

    @Test
    void testFullAiOrchestrationPipeline() {
        // Given
        String ticketId = UUID.randomUUID().toString();
        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(100L); event.setTicketNumber(ticketId);
        event.setSubject("End-to-End Orchestration Test");
        event.setMessage("Test full AI execution cycle");

        // When
        kafkaTemplate.send("ticket-analyzed", ticketId, event);

        // Then
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            WorkflowExecutionEntity execution = workflowExecutionRepository.findAll().stream()
                    .filter(e -> ticketId.equals(e.getCorrelationId()))
                    .findFirst()
                    .orElse(null);
            
            assertThat(execution).isNotNull();
            assertThat(execution.getState().name()).isIn("COMPLETED", "FAILED");

            List<AiExecutionRecordEntity> auditRecords = aiExecutionRecordRepository.findAll().stream()
                    .filter(a -> a.getCorrelationId().equals(execution.getCorrelationId()))
                    .toList();
            
            assertThat(auditRecords).isNotEmpty();
            assertThat(auditRecords.get(0).getOutcome()).isEqualTo("SUCCESS");
            
            // 3. Verify Outbox / Messaging (Implicitly we assume outbox listener fires here)
            // A dedicated test consumer would pull from "ticket-resolved" and assert payload.
        });
    }
}













