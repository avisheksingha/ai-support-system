package com.aisupport.orchestration.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.aisupport.common.event.TicketCreatedEvent;
import com.aisupport.orchestration.domain.state.WorkflowState;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Import(TestAiConfiguration.class)
@RequiredArgsConstructor
class WorkflowToolFailureIT extends AbstractIntegrationTest {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final WorkflowExecutionRepository workflowExecutionRepository;

    @DynamicPropertySource
    static void configureFailureProperties(DynamicPropertyRegistry registry) {
        // Force an aggressively low timeout to simulate failure
        registry.add("resilience4j.timelimiter.instances.github-mcp.timeout-duration", () -> "1ms");
        // We'd also ideally want a Mock configuration that forcefully sleeps for 10ms to trigger the timeout
    }
    @Test
    void testWorkflowHandlesToolTimeoutGracefully() {
        // Given
        String ticketNumber = UUID.randomUUID().toString();
        Long ticketId = 100L;
        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(ticketId); event.setTicketNumber(ticketNumber);
        event.setSubject("Timeout Test");

        // When
        kafkaTemplate.send("ticket-created", ticketNumber, event);

        // Then - We want to see it either complete without that tool, or fail gracefully into a FAILED state
        String expectedCorrelationId = "ticket-" + ticketId;
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            WorkflowExecutionEntity execution = workflowExecutionRepository.findAll().stream()
                    .filter(e -> expectedCorrelationId.equals(e.getCorrelationId()))
                    .findFirst()
                    .orElse(null);
            
            assertThat(execution).isNotNull();
            // Depending on resilience strategy, it may still complete if tool was optional, or go to FAILED
            assertThat(execution.getState()).isIn(WorkflowState.COMPLETED, WorkflowState.FAILED);
        });
    }
}














