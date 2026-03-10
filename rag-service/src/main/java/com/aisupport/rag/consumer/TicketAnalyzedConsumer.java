package com.aisupport.rag.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aisupport.rag.event.TicketAnalyzedEvent;
import com.aisupport.rag.service.RetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAnalyzedConsumer {

    private final RetrievalService retrievalService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ticket-analyzed", groupId = "rag-group")
    public void consume(String payload) {

        try {

            TicketAnalyzedEvent event = objectMapper.readValue(payload, TicketAnalyzedEvent.class);

            log.info("RAG received ticket {}", event.getTicketId());

            String text =
                    event.getIntent() + " " +
                    String.join(" ", event.getKeywords());

            var articles = retrievalService.retrieveRelevant(text);

            log.info("Top matches found: {}", articles.size());

        } catch (Exception e) {

            log.error("Failed to process analyzed payload={}", payload, e);
        }
    }
}
