package com.aisupport.rag.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.aisupport.rag.entity.KnowledgeArticle;
import com.aisupport.rag.repository.KnowledgeArticleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core embedding pipeline: reads un-embedded articles, chunks them,
 * embeds them into PGVector, and flags them as embedded.
 *
 * Kept separate from any trigger mechanism (startup runner, scheduled job,
 * admin endpoint) so the logic is reusable and independently testable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeEmbeddingService {

    private final KnowledgeArticleRepository repo;
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    /**
     * Embeds all currently un-embedded articles.
     * @return number of articles successfully embedded and flagged
     */
    public int embedPendingArticles() {
        List<KnowledgeArticle> unembeddedArticles = repo.findByEmbeddedFalse();
        if (unembeddedArticles.isEmpty()) {
            return 0;
        }

        log.info("Loading {} un-embedded knowledge articles into vector store...", unembeddedArticles.size());

        List<Document> rawDocs = unembeddedArticles.stream()
            .map(article -> Document.builder()
                .text(article.getTitle() + ": " + article.getContent())
                .metadata(Map.of("articleId", article.getId(), "title", article.getTitle()))
                .build())
            .toList();

        List<Document> chunkedDocs = textSplitter.apply(rawDocs);
        log.info("Split into {} chunks for embedding.", chunkedDocs.size());

        List<Long> embeddedIds = unembeddedArticles.stream()
            .map(KnowledgeArticle::getId)
            .toList();

        try {
            vectorStore.add(chunkedDocs);
            repo.markArticlesAsEmbedded(embeddedIds);
        } catch (Exception ex) {
            log.error("Embedding failed after vector store insert for article IDs {}. " +
                "Check vector_store for possible duplicates before retrying.", embeddedIds, ex);
            throw ex;
        }

        return embeddedIds.size();
    }
}