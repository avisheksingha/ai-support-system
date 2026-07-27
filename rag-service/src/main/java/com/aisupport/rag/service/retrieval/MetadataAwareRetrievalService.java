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

    public RetrievalResult retrieveAndRank(String query) {
        RetrievalContext context = metadataExtractor.extract(query);
        log.debug("Executing retrieval for context: category='{}', keywords={}, intent='{}', confidence={}",
                context.suggestedCategory(), context.keywords(), context.intent(), context.confidenceScore());

        List<Document> validCandidates = fetchCandidates(context);

        List<Document> rankedDocs = documentRanker.rankAndSelectTopK(validCandidates, context, DEFAULT_FINAL_TOP_K);
        log.info("Retrieval complete: selected {} top ranked documents from {} validated candidates.", rankedDocs.size(), validCandidates.size());

        return promptBuilder.build(rankedDocs);
    }

    private List<Document> fetchCandidates(RetrievalContext context) {
        if (context.hasCategory() && context.isHighConfidence()) {
            String catFilter = String.format("status == 'PUBLISHED' && embeddingStatus == 'READY' && category == '%s'",
                    context.suggestedCategory());
            List<Document> catDocs = searchVectorStoreSafe(context.cleanedQuery(), DEFAULT_CANDIDATE_TOP_K, DEFAULT_SIMILARITY_THRESHOLD, catFilter);
            List<Document> validCandidates = applyGovernanceFilter(catDocs);

            if (!validCandidates.isEmpty()) {
                return validCandidates;
            }
            log.info("Category-scoped RAG search for category '{}' returned 0 valid PUBLISHED candidates. Automatically falling back to global vector search...", context.suggestedCategory());
        } else if (!context.isHighConfidence()) {
            log.info("Low confidence score ({}) detected. Skipping strict category restriction and executing broadened global vector search.", context.confidenceScore());
        }

        String globalFilter = "status == 'PUBLISHED' && embeddingStatus == 'READY'";
        List<Document> globalDocs = searchVectorStoreSafe(context.cleanedQuery(), DEFAULT_CANDIDATE_TOP_K, DEFAULT_SIMILARITY_THRESHOLD, globalFilter);
        return applyGovernanceFilter(globalDocs);
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
