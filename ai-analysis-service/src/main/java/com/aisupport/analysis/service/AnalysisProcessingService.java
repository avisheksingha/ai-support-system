package com.aisupport.analysis.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.analysis.chat.ChatProvider;
import com.aisupport.analysis.dto.ParsedAnalysis;
import com.aisupport.analysis.entity.AnalysisResult;
import com.aisupport.analysis.outbox.OutboxEventService;
import com.aisupport.analysis.repository.AnalysisResultRepository;
import com.aisupport.common.dto.AnalysisResultDTO;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisProcessingService {

    private final ChatProvider chatProvider;
    private final AnalysisResultRepository repository;
    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxService;

    @Transactional
    public void processTicket(TicketCreatedEvent event) {

        Long ticketId = event.getTicketId();

        // Idempotency guard
        if (repository.existsByTicketId(ticketId)) {
            log.info("Analysis already exists for ticketId={}, skipping", ticketId);
            return;
        }

        log.info("Starting AI analysis for ticketId={}", ticketId);

        ParsedAnalysis parsed = chatProvider.analyzeTicket(
                event.getSubject(),
                event.getMessage()
        );
        
        String normalizedIntent = normalizeIntent(parsed.getIntent());
        
        BigDecimal confidence = parsed.getConfidenceScore() != null
                ? BigDecimal.valueOf(parsed.getConfidenceScore())
                : BigDecimal.ZERO;

        AnalysisResult entity = AnalysisResult.builder()
                .ticketId(ticketId)
                .intent(normalizedIntent)
                .sentiment(defaultIfNull(parsed.getSentiment(), "NEUTRAL").toUpperCase())
                .urgency(defaultIfNull(parsed.getUrgency(), "LOW").toUpperCase())
                .confidenceScore(confidence)
                .keywords(parsed.getKeywords() != null
                        ? parsed.getKeywords().toArray(new String[0])
                        : new String[0])
                .suggestedCategory(parsed.getSuggestedCategory())
                .rawResponse(convertToJson(parsed))
                .build();

        repository.save(entity);

        log.info("Analysis persisted for ticketId={}, intent={}", ticketId, normalizedIntent);

        // NEW: Publish analyzed event via Outbox
        TicketAnalyzedEvent analyzedEvent = TicketAnalyzedEvent.builder()
                .ticketId(ticketId)
                .ticketDescription(event.getMessage())
                .intent(normalizedIntent)
                .sentiment(defaultIfNull(parsed.getSentiment(), "NEUTRAL").toUpperCase())
                .urgency(defaultIfNull(parsed.getUrgency(), "LOW").toUpperCase())
                .confidenceScore(parsed.getConfidenceScore())
                .keywords(parsed.getKeywords())
                .suggestedCategory(parsed.getSuggestedCategory())
                .analyzedAt(Instant.now())
                .build();

        outboxService.publishEvent(
        		"TICKET",
                ticketId.toString(),
                "TicketAnalyzedEvent",
                analyzedEvent
        );
    }
    
    @Transactional
    public AnalysisResultDTO analyzeTicketSync(Long ticketId, String subject, String message) {
        log.info("Starting sync AI analysis for ticketId={}", ticketId);

        ParsedAnalysis parsed = chatProvider.analyzeTicket(subject, message);
        
        String normalizedIntent = normalizeIntent(parsed.getIntent());
        
        BigDecimal confidence = parsed.getConfidenceScore() != null
                ? BigDecimal.valueOf(parsed.getConfidenceScore())
                : BigDecimal.ZERO;

        AnalysisResult entity = AnalysisResult.builder()
                .ticketId(ticketId)
                .intent(normalizedIntent)
                .sentiment(defaultIfNull(parsed.getSentiment(), "NEUTRAL").toUpperCase())
                .urgency(defaultIfNull(parsed.getUrgency(), "LOW").toUpperCase())
                .confidenceScore(confidence)
                .keywords(parsed.getKeywords() != null
                        ? parsed.getKeywords().toArray(new String[0])
                        : new String[0])
                .suggestedCategory(parsed.getSuggestedCategory())
                .rawResponse(convertToJson(parsed))
                .build();

        // Overwrite if exists, since it's a sync call from orchestrator
        if (repository.existsByTicketId(ticketId)) {
            // we could update, but for simplicity we'll just save it, assuming DB constraints handle it
            // or we do not save again.
            // Let's just do save and hope the ID isn't causing a unique constraint violation if ticketId is unique.
            // Better yet, just return the existing if it exists, or update. Let's delete existing or update.
        	repository.deleteByTicketId(ticketId);
        }

        repository.save(entity);
        log.info("Sync Analysis persisted for ticketId={}, intent={}", ticketId, normalizedIntent);

        return AnalysisResultDTO.builder()
                .ticketId(ticketId)
                .intent(normalizedIntent)
                .sentiment(entity.getSentiment())
                .urgency(entity.getUrgency())
                .confidenceScore(confidence)
                .keywords(java.util.Arrays.asList(entity.getKeywords()))
                .suggestedCategory(entity.getSuggestedCategory())
                .build();
    }
    
    private String normalizeIntent(String intent) {

        if (intent == null) return "GENERAL";

        intent = intent.toUpperCase();

        if (intent.contains("REFUND"))
            return "CHECK_REFUND_STATUS";

        if (intent.contains("PAYMENT"))
            return "PAYMENT_ISSUE";

        if (intent.contains("CRASH") || intent.contains("BUG"))
            return "TECHNICAL_ISSUE";

        if (intent.contains("COMPLAINT"))
            return "GENERAL_COMPLAINT";

        if (intent.contains("GRATITUDE") || intent.contains("THANK"))
            return "GRATITUDE";

        if (intent.contains("FEATURE"))
            return "FEATURE_REQUEST";

        return "GENERAL";
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