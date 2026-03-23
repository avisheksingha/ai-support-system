package com.aisupport.ticket.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.aisupport.common.event.TicketCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

class OutboxEventPublisherTest {

    private OutboxEventRepository repository;
    private ObjectMapper objectMapper;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private OutboxEventPublisher publisher;

    @BeforeEach
    void setUp() {
        repository = mock(OutboxEventRepository.class);
        objectMapper = mock(ObjectMapper.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new OutboxEventPublisher(repository, objectMapper, kafkaTemplate);
    }

    @Test
    void publishEvents_shouldRetryFailedEventsBelowRetryLimit() throws Exception {
        OutboxEvent failedEvent = OutboxEvent.builder()
                .id("evt-1")
                .aggregateType("TICKET")
                .aggregateId("42")
                .eventType("TicketCreatedEvent")
                .payload("{\"ticketId\":42}")
                .status(OutboxEvent.Status.FAILED)
                .retryCount(1)
                .build();
        failedEvent.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        when(repository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEvent.Status.PENDING))
                .thenReturn(List.of());
        when(repository.findByStatusAndRetryCountLessThan(
                OutboxEvent.Status.FAILED, OutboxEvent.MAX_RETRIES))
                .thenReturn(List.of(failedEvent));

        TicketCreatedEvent payload = TicketCreatedEvent.builder()
                .ticketId(42L)
                .ticketNumber("TKT-42")
                .subject("subject")
                .message("message")
                .createdAt(LocalDateTime.now())
                .build();

        when(objectMapper.readValue(anyString(), eq(TicketCreatedEvent.class)))
                .thenReturn(payload);

        CompletableFuture<SendResult<String, Object>> result =
                CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(result);

        publisher.publishEvents();

        verify(repository).findByStatusAndRetryCountLessThan(
                OutboxEvent.Status.FAILED, OutboxEvent.MAX_RETRIES);
        assertThat(failedEvent.getStatus()).isEqualTo(OutboxEvent.Status.SENT);
        assertThat(failedEvent.getProcessedAt()).isNotNull();
    }
}
