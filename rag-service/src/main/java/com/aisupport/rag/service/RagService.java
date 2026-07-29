package com.aisupport.rag.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.event.EventType;
import com.aisupport.common.event.TicketRagResponseEvent;
import com.aisupport.rag.entity.RagResponse;
import com.aisupport.rag.exception.RagGenerationException;
import com.aisupport.rag.outbox.OutboxEventService;
import com.aisupport.rag.repository.KnowledgeArticleRepository;
import com.aisupport.rag.repository.RagResponseRepository;
import com.aisupport.rag.service.retrieval.MetadataAwareRetrievalService;
import com.aisupport.rag.service.retrieval.RagPromptFactory;
import com.aisupport.rag.service.retrieval.RetrievalDiagnostics;
import com.aisupport.rag.service.retrieval.RetrievalResult;
import com.aisupport.rag.service.serialization.SourceMetadataSerializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service orchestration layer responsible for RAG response generation, persistence,
 * and outbox event publishing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final ChatClient chatClient;
    private final MetadataAwareRetrievalService retrievalService;
    private final RagResponseRepository ragResponseRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final OutboxEventService outboxEventService;
    private final RagPromptFactory ragPromptFactory;
    private final SourceMetadataSerializer sourceSerializer;

    @Value("${spring.ai.google.genai.chat.model}")
    private String chatModel;

    private record RagExecutionResult(
        String response,
        int docCount,
        String titles,
        String sourceDetails,
        String retrievalDiagnostics,
        RetrievalResult retrievalResult,
        boolean knowledgeFound,
        String model,
        long totalDurationMs
    ) {}

    /**
     * Executes the core retrieval, prompt construction, LLM generation, and access count updates.
     * Consolidates logic shared between sync and async RAG workflows.
     */
    private RagExecutionResult executeRagWorkflow(Long ticketId, String query) {
        long startTime = System.currentTimeMillis();
        try {
            RetrievalResult retrievalResult = retrievalService.retrieveAndRank(query);
            String fullSystemPrompt = ragPromptFactory.buildFullSystemPrompt(retrievalResult);

            ChatResponse chatResponse = chatClient.prompt()
                .system(fullSystemPrompt)
                .user(query)
                .call()
                .chatResponse();

            if (chatResponse == null || chatResponse.getResult() == null || chatResponse.getResult().getOutput() == null) {
                throw new IllegalStateException("Empty or null chat response received for ticketId: " + ticketId);
            }

            String response = chatResponse.getResult().getOutput().getText();
            int docCount = retrievalResult.retrievedDocumentCount();
            String titles = retrievalResult.matchedArticleTitles();
            String sourceDetails = sourceSerializer.serializeSources(retrievalResult.documents());
            String diagnosticsJson = sourceSerializer.serializeDiagnostics(retrievalResult.diagnostics());

            // Derive knowledgeFound directly from RetrievalResult doc count (Objective 5)
            boolean knowledgeFound = docCount > 0;

            // Update access count using immutable Article IDs instead of titles (Objective 4)
            List<Long> matchedIds = retrievalResult.matchedArticleIds();
            if (knowledgeFound && !matchedIds.isEmpty()) {
                incrementAccessCountSafeByIds(matchedIds);
            } else if (knowledgeFound && titles != null && !titles.isEmpty()) {
                // Backward compatibility fallback if IDs are unavailable
                incrementAccessCountSafeByTitles(List.of(titles.split(",")));
            }

            long totalDurationMs = System.currentTimeMillis() - startTime;

            // Structured logging with rich retrieval metadata (Objective 8)
            RetrievalDiagnostics diag = retrievalResult.diagnostics();
            log.info("RAG workflow completed for ticketId={}, docCount={}, fallbackUsed={}, retrievalLatencyMs={}, totalDurationMs={}, strategy='{}', knowledgeFound={}",
                    ticketId, docCount, diag != null && diag.fallbackUsed(),
                    diag != null ? diag.retrievalLatencyMs() : 0, totalDurationMs,
                    diag != null ? diag.retrievalStrategy() : "UNKNOWN", knowledgeFound);

            return new RagExecutionResult(
                    response, docCount, titles, sourceDetails, diagnosticsJson,
                    retrievalResult, knowledgeFound, chatModel, totalDurationMs
            );
        } catch (Exception e) {
            long failDuration = System.currentTimeMillis() - startTime;
            log.error("Google GenAI RAG generation failed for ticketId={}, durationMs={}", ticketId, failDuration, e);
            throw new RagGenerationException("RAG generation failed for ticketId: " + ticketId, e);
        }
    }

    /**
     * Generate a context-aware response for the given query using RAG, persist it, and emit an event.
     *
     * @param ticketId ticket identifier associated with the query
     * @param query the search query (typically built from ticket analysis fields)
     * @return the AI-generated response grounded in knowledge base context
     */
    @Transactional
    public String generateResponse(Long ticketId, String query) {
        log.info("Running RAG for query: {}", query);
        RagExecutionResult exec = executeRagWorkflow(ticketId, query);

        RagResponse ragResponse = RagResponse.builder()
                .ticketId(ticketId)
                .query(query)
                .response(exec.response())
                .model(exec.model())
                .knowledgeFound(exec.knowledgeFound())
                .retrievedDocumentCount(exec.docCount())
                .matchedArticleTitles(exec.titles())
                .sourceDetails(exec.sourceDetails())
                .retrievalDiagnostics(exec.retrievalDiagnostics())
                .build();

        ragResponseRepository.save(ragResponse);
        log.info("RAG response persisted for ticketId={}", ticketId);

        TicketRagResponseEvent event = TicketRagResponseEvent.builder()
                .ticketId(ticketId)
                .query(query)
                .response(exec.response())
                .model(exec.model())
                .generatedAt(Instant.now())
                .build();

        outboxEventService.publishEvent(
                "TICKET",
                ticketId.toString(),
                EventType.TICKET_RAG_RESPONSE_GENERATED,
                event
        );
        log.info("RAG response event published for ticketId={}", ticketId);

        return exec.response();
    }

    /**
     * Synchronously generates and persists a RAG response without publishing an outbox event.
     */
    @Transactional
    public RagResponse generateResponseSync(Long ticketId, String query) {
        log.info("Running sync RAG for query: {}", query);
        RagExecutionResult exec = executeRagWorkflow(ticketId, query);

        if (ragResponseRepository.existsById(ticketId)) {
            ragResponseRepository.deleteById(ticketId);
        }

        RagResponse ragResponse = RagResponse.builder()
                .ticketId(ticketId)
                .query(query)
                .response(exec.response())
                .model(exec.model())
                .knowledgeFound(exec.knowledgeFound())
                .retrievedDocumentCount(exec.docCount())
                .matchedArticleTitles(exec.titles())
                .sourceDetails(exec.sourceDetails())
                .retrievalDiagnostics(exec.retrievalDiagnostics())
                .build();

        ragResponseRepository.save(ragResponse);
        log.info("Sync RAG response persisted for ticketId={}", ticketId);

        return ragResponse;
    }

    /**
     * Retrieves the most recent RAG response for a given ticket.
     */
    public Optional<RagResponse> getRagResponseForTicket(Long ticketId) {
        return ragResponseRepository.findTopByTicketIdOrderByCreatedAtDesc(ticketId);
    }

    private void incrementAccessCountSafeByIds(List<Long> matchedIds) {
        try {
            knowledgeArticleRepository.incrementAccessCountByIds(matchedIds);
        } catch (Exception e) {
            log.warn("Failed to increment access count for article IDs: {}", matchedIds, e);
        }
    }

    private void incrementAccessCountSafeByTitles(List<String> matchedTitles) {
        try {
            knowledgeArticleRepository.incrementAccessCountByTitles(matchedTitles);
        } catch (Exception e) {
            log.warn("Failed to increment access count for titles: {}", matchedTitles, e);
        }
    }
}
