package com.aisupport.rag.consumer;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aisupport.rag.event.TicketAnalyzedEvent;
import com.aisupport.rag.service.RagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAnalyzedConsumer {

    private final RagService ragService;

    @KafkaListener(topics = "ticket-analyzed", groupId = "rag-group")
    public void consume(TicketAnalyzedEvent event) {

        log.info("RAG received ticket {}", event.getTicketId());

        String query = String.join(" ",
                safe(event.getIntent()),
                safe(event.getSentiment()),
                safe(event.getUrgency()),
                String.join(" ", safeList(event.getKeywords()))
        );

        String response = ragService.generateResponse(query);

        log.info("RAG answer: {}", response);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private List<String> safeList(List<String> list) {
        return list == null ? List.of() : list;
    }
}