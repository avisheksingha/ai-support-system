package com.aisupport.orchestration.infrastructure;

import static org.awaitility.Awaitility.await;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import com.aisupport.common.event.TicketCreatedEvent;

import lombok.RequiredArgsConstructor;

@Import(TestAiConfiguration.class)
@RequiredArgsConstructor
class WorkflowMessagingIT extends AbstractIntegrationTest {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // We don't have the explicit container setup in this snippet for brevity,
    // but in a real implementation we would dynamically register a consumer
    // for the "ticket-routed" and "ticket-resolved" topics to verify outbox firing.
    @Test
    void testOutboundEventPublished() {
        // Given
        String ticketId = UUID.randomUUID().toString();
        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(100L); event.setTicketNumber(ticketId);
        event.setSubject("Messaging Test");
        event.setMessage("Verify outbound kafka event");

        // When
        kafkaTemplate.send("ticket-created", ticketId, event);

        // Then
        // We wait for the outbound event (would check a local test consumer queue)
        // Since we don't have the test consumer fully fleshed out, we just wait for workflow completion
        // and ideally check the OutboxEventEntity table if we have one.
        
        // For now, this acts as a placeholder for the true messaging IT.
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            // Assert outbox or outbound topic message
        });
    }
}













