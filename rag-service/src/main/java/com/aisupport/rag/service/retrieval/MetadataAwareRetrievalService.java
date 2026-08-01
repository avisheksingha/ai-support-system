package com.aisupport.rag.service.retrieval;

import java.util.List;
import java.util.Objects;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.aisupport.rag.entity.EmbeddingStatus;
import com.aisupport.rag.entity.KnowledgeArticleStatus;
import com.aisupport.rag.repository.KnowledgeArticleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core enterprise RAG retrieval engine.
 * Orchestrates metadata extraction, confidence-aware candidate retrieval with automatic fallback,
 * live database governance pre-filtering, hybrid ranking, and structured metadata prompt generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataAwareRetrievalService {

    private final VectorStore vectorStore;
    private final KnowledgeArticleRepository articleRepo;
    private final QueryMetadataExtractor metadataExtractor;
    private final HybridDocumentRanker documentRanker;
    private final MetadataPromptBuilder promptBuilder;

    private static final int DEFAULT_CANDIDATE_TOP_K = 20;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.40;
    private static final int DEFAULT_FINAL_TOP_K = 5;

    private record CandidateExecution(List<Document> candidates, boolean fallbackUsed, String strategy) {}

    public RetrievalResult retrieveAndRank(String query) {
        long startTime = System.currentTimeMillis();
        RetrievalContext context = metadataExtractor.extract(query);
        log.debug("Executing retrieval for context: category='{}', keywords={}, intent='{}', confidence={}",
                context.suggestedCategory(), context.keywords(), context.intent(), context.confidenceScore());

        CandidateExecution exec = fetchCandidates(context);
        List<Document> validCandidates = exec.candidates();

        List<Document> rankedDocs = documentRanker.rankAndSelectTopK(validCandidates, context, DEFAULT_FINAL_TOP_K);
        long latencyMs = System.currentTimeMillis() - startTime;
        log.info("Retrieval complete: selected {} top ranked documents from {} validated candidates in {}ms (strategy={}).",
                rankedDocs.size(), validCandidates.size(), latencyMs, exec.strategy());

        boolean catMatched = rankedDocs.stream().anyMatch(d -> d.getMetadata().get("categoryScore") instanceof Number n && n.doubleValue() > 0);
        boolean tagMatched = rankedDocs.stream().anyMatch(d -> d.getMetadata().get("tagScore") instanceof Number n && n.doubleValue() > 0);
        boolean kwMatched = rankedDocs.stream().anyMatch(d -> d.getMetadata().get("matchedKeywords") != null && !d.getMetadata().get("matchedKeywords").toString().isEmpty());

        RetrievalDiagnostics diagnostics = new RetrievalDiagnostics(
                rankedDocs.size(),
                exec.fallbackUsed(),
                latencyMs,
                catMatched,
                kwMatched,
                tagMatched,
                context.confidenceScore() != null ? context.confidenceScore() : 0.0,
                exec.strategy()
        );

        return promptBuilder.build(rankedDocs, diagnostics);
    }

    private CandidateExecution fetchCandidates(RetrievalContext context) {
        if (context.hasCategory() && context.isHighConfidence()) {
            String catFilter = String.format("status == 'PUBLISHED' && embeddingStatus == 'READY' && category == '%s'",
                    context.suggestedCategory());
            List<Document> catDocs = searchVectorStoreSafe(context.cleanedQuery(), DEFAULT_CANDIDATE_TOP_K, DEFAULT_SIMILARITY_THRESHOLD, catFilter);
            List<Document> validCandidates = applyGovernanceFilter(catDocs);

            if (!validCandidates.isEmpty()) {
                return new CandidateExecution(validCandidates, false, "CATEGORY_SCOPED");
            }
            log.info("Category-scoped RAG search for category '{}' returned 0 valid PUBLISHED candidates. Automatically falling back to global vector search...", context.suggestedCategory());
            String globalFilter = "status == 'PUBLISHED' && embeddingStatus == 'READY'";
            List<Document> globalDocs = searchVectorStoreSafe(context.cleanedQuery(), DEFAULT_CANDIDATE_TOP_K, DEFAULT_SIMILARITY_THRESHOLD, globalFilter);
            return new CandidateExecution(applyGovernanceFilter(globalDocs), true, "GLOBAL_FALLBACK");
        } else if (!context.isHighConfidence()) {
            log.info("Low confidence score ({}) detected. Skipping strict category restriction and executing broadened global vector search.", context.confidenceScore());
        }

        String globalFilter = "status == 'PUBLISHED' && embeddingStatus == 'READY'";
        List<Document> globalDocs = searchVectorStoreSafe(context.cleanedQuery(), DEFAULT_CANDIDATE_TOP_K, DEFAULT_SIMILARITY_THRESHOLD, globalFilter);
        return new CandidateExecution(applyGovernanceFilter(globalDocs), false, "BROADER_GLOBAL");
    }

    private List<Document> applyGovernanceFilter(List<Document> rawCandidates) {
        if (rawCandidates == null || rawCandidates.isEmpty()) {
            return List.of();
        }

        List<Document> inMemoryValid = filterInMemory(rawCandidates);
        List<Long> candidateIds = extractArticleIds(inMemoryValid);

        if (candidateIds.isEmpty() && !inMemoryValid.isEmpty()) {
            return inMemoryValid;
        }
        if (candidateIds.isEmpty()) {
            return List.of();
        }

        List<Long> liveValidIds = articleRepo.findIdsByStatusAndEmbeddingStatusAndIdIn(
                KnowledgeArticleStatus.PUBLISHED, EmbeddingStatus.READY, candidateIds);

        return inMemoryValid.stream()
                .filter(d -> isLiveValid(d, liveValidIds))
                .toList();
    }

    private List<Document> filterInMemory(List<Document> rawCandidates) {
        return rawCandidates.stream()
                .filter(this::isValidMetadata)
                .toList();
    }

    private boolean isValidMetadata(Document d) {
        Object st = d.getMetadata().get("status");
        Object embSt = d.getMetadata().get("embeddingStatus");
        boolean statusOk = (st == null || "PUBLISHED".equalsIgnoreCase(st.toString()));
        boolean embOk = (embSt == null || "READY".equalsIgnoreCase(embSt.toString()));
        if (!statusOk || !embOk) {
            log.debug("Discarding candidate doc ID={} due to in-memory metadata check (status={}, embeddingStatus={})", d.getId(), st, embSt);
        }
        return statusOk && embOk;
    }

    private List<Long> extractArticleIds(List<Document> docs) {
        return docs.stream()
                .map(d -> d.getMetadata().get("articleId"))
                .filter(Objects::nonNull)
                .map(obj -> {
                    try {
                        return Long.valueOf(obj.toString());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private boolean isLiveValid(Document d, List<Long> liveValidIds) {
        Object idObj = d.getMetadata().get("articleId");
        if (idObj == null) {
            return true;
        }
        try {
            Long artId = Long.valueOf(idObj.toString());
            return liveValidIds.contains(artId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<Document> searchVectorStoreSafe(String queryText, int topK, double threshold, String filterExpression) {
        try {
            SearchRequest req = SearchRequest.builder()
                    .query(queryText)
                    .topK(topK)
                    .similarityThreshold(threshold)
                    .filterExpression(filterExpression)
                    .build();
            return vectorStore.similaritySearch(req);
        } catch (Exception ex) {
            log.warn("Vector store similarity search with filterExpression '{}' failed (likely test mock or indexing constraint). Retrying without filter expression and applying in-memory governance...", filterExpression, ex);
            try {
                SearchRequest fallbackReq = SearchRequest.builder()
                        .query(queryText)
                        .topK(topK)
                        .similarityThreshold(threshold)
                        .build();
                return vectorStore.similaritySearch(fallbackReq);
            } catch (Exception ex2) {
                log.error("Vector store similarity search failed completely for query: {}", queryText, ex2);
                return List.of();
            }
        }
    }
}
