package com.aisupport.ticket.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.aisupport.common.event.TicketCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OutboxEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OutboxEventPublisher(repository, objectMapper, kafkaTemplate);
    }

    @Test
    void publishEvents_shouldRetryFailedEventsBelowRetryLimit() throws JsonProcessingException {
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

        SendResult<String, Object> sendResult = new SendResult<>(
                new ProducerRecord<>("ticket-created", "evt-1", payload),
                null);
        CompletableFuture<SendResult<String, Object>> result =
                CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(ArgumentMatchers.<ProducerRecord<String, Object>>any()))
                .thenReturn(result);

        publisher.publishEvents();

        verify(repository).findByStatusAndRetryCountLessThan(
                OutboxEvent.Status.FAILED, OutboxEvent.MAX_RETRIES);
        assertThat(failedEvent.getStatus()).isEqualTo(OutboxEvent.Status.SENT);
        assertThat(failedEvent.getProcessedAt()).isNotNull();
    }
}
