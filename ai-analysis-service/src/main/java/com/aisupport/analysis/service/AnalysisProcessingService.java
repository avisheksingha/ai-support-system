package com.aisupport.analysis.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.analysis.dto.ParsedAnalysis;
import com.aisupport.analysis.entity.AnalysisResult;
import com.aisupport.analysis.event.TicketAnalyzedEvent;
import com.aisupport.analysis.event.TicketCreatedEvent;
import com.aisupport.analysis.outbox.OutboxEventService;
import com.aisupport.analysis.repository.AnalysisResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisProcessingService {

    private final AiProvider aiProvider;
    private final AnalysisResultRepository repository;
    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxService;

    @Transactional
    public void processTicket(TicketCreatedEvent event) {

        Long ticketId = event.getTicketId();

        // NEW: Idempotency guard
        if (repository.existsByTicketId(ticketId)) {
            log.info("Analysis already exists for ticketId={}, skipping", ticketId);
            return;
        }

        log.info("Starting AI analysis for ticketId={}", ticketId);

        ParsedAnalysis parsed = aiProvider.analyzeTicket(
                event.getSubject(),
                event.getMessage()
        );
        
        BigDecimal confidence = parsed.getConfidenceScore() != null
                ? BigDecimal.valueOf(parsed.getConfidenceScore())
                : BigDecimal.ZERO;

        AnalysisResult entity = AnalysisResult.builder()
                .ticketId(ticketId)
                .intent(defaultIfNull(parsed.getIntent(), "GENERAL"))
                .sentiment(defaultIfNull(parsed.getSentiment(), "NEUTRAL"))
                .urgency(defaultIfNull(parsed.getUrgency(), "LOW"))
                .confidenceScore(confidence)
                .keywords(parsed.getKeywords() != null
                        ? parsed.getKeywords().toArray(new String[0])
                        : new String[0])
                .suggestedCategory(parsed.getSuggestedCategory())
                .rawResponse(convertToJson(parsed))
                .build();

        repository.save(entity);

        log.info("Analysis persisted for ticketId={}", ticketId);

        // NEW: Publish analyzed event using Outbox
        TicketAnalyzedEvent analyzedEvent = TicketAnalyzedEvent.builder()
                .ticketId(ticketId)
                .intent(parsed.getIntent())
                .sentiment(parsed.getSentiment())
                .urgency(parsed.getUrgency())
                .confidenceScore(parsed.getConfidenceScore())
                .keywords(parsed.getKeywords())
                .suggestedCategory(parsed.getSuggestedCategory())
                .analyzedAt(LocalDateTime.now())
                .build();

        outboxService.publishEvent(
                "ticket-analyzed",
                ticketId.toString(),
                analyzedEvent
        );
    }

    private String convertToJson(ParsedAnalysis analysis) {
        try {
            return objectMapper.writeValueAsString(analysis);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String defaultIfNull(String value, String fallback) {
        return value != null ? value : fallback;
    }
}