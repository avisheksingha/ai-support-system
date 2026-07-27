package com.aisupport.rag.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.rag.entity.EmbeddingStatus;
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
    private final JdbcTemplate jdbcTemplate;

    /**
     * Embeds all currently un-embedded articles.
     * @return number of articles successfully embedded and flagged
     */
    @Transactional
    public int embedPendingArticles() {
        List<KnowledgeArticle> unembeddedArticles = repo.findByEmbeddingStatus(EmbeddingStatus.PENDING);
        if (unembeddedArticles.isEmpty()) {
            return 0;
        }

        // Mark as PROCESSING immediately
        List<Long> articleIds = unembeddedArticles.stream()
            .map(KnowledgeArticle::getId)
            .toList();
        repo.updateEmbeddingStatus(articleIds, EmbeddingStatus.PROCESSING);

        log.info("Loading {} un-embedded knowledge articles into vector store...", unembeddedArticles.size());

        List<Document> rawDocs = unembeddedArticles.stream()
            .map(article -> {
                Map<String, Object> meta = new HashMap<>();
                meta.put("articleId", article.getId());
                meta.put("title", article.getTitle() != null ? article.getTitle() : "Unknown");
                meta.put("category", article.getCategory() != null ? article.getCategory() : "");
                meta.put("tags", article.getTags() != null ? String.join(",", article.getTags()) : "");
                meta.put("status", article.getStatus() != null ? article.getStatus().name() : "DRAFT");
                meta.put("embeddingStatus", article.getEmbeddingStatus() != null ? article.getEmbeddingStatus().name() : "PENDING");
                meta.put("version", article.getVersion() != null ? article.getVersion() : 1L);
                return Document.builder()
                    .text(article.getTitle() + ": " + article.getContent())
                    .metadata(meta)
                    .build();
            })
            .toList();

        List<Document> rawChunkedDocs = textSplitter.apply(rawDocs);
        Map<Object, Integer> chunkCounters = new HashMap<>();
        List<Document> chunkedDocs = rawChunkedDocs.stream()
            .map(doc -> {
                Object artId = doc.getMetadata().get("articleId");
                int idx = chunkCounters.getOrDefault(artId, 0);
                chunkCounters.put(artId, idx + 1);
                Map<String, Object> newMeta = new HashMap<>(doc.getMetadata());
                newMeta.put("chunkIndex", idx);
                return Document.builder()
                    .id(doc.getId())
                    .text(doc.getText())
                    .metadata(newMeta)
                    .build();
            })
            .toList();
        log.info("Split into {} chunks for embedding.", chunkedDocs.size());

        List<Long> embeddedIds = unembeddedArticles.stream()
            .map(KnowledgeArticle::getId)
            .toList();

        try {
            vectorStore.add(chunkedDocs);
            repo.updateEmbeddingStatus(embeddedIds, EmbeddingStatus.READY);
        } catch (Exception ex) {
            repo.updateEmbeddingStatus(embeddedIds, EmbeddingStatus.FAILED);
            log.error("Embedding failed after vector store insert for article IDs {}. " +
                "Check vector_store for possible duplicates before retrying.", embeddedIds, ex);
            throw ex;
        }

        return embeddedIds.size();
    }

    /**
     * Synchronizes metadata changes directly to PGVector without re-triggering embedding generation.
     */
    @Transactional
    public void syncVectorMetadata(Long articleId, String status, String category, List<String> tags, Long version) {
        if (articleId == null) return;
        String tagsStr = tags != null ? String.join(",", tags) : "";
        String sql = """
            UPDATE vector_store 
            SET metadata = (metadata::jsonb || jsonb_build_object(
                'status', ?, 'category', ?, 'tags', ?, 'version', ?
            ))::json
            WHERE (metadata->>'articleId')::bigint = ?
            """;
        try {
            int updated = jdbcTemplate.update(sql, status != null ? status : "DRAFT", 
                                                   category != null ? category : "", 
                                                   tagsStr, 
                                                   version != null ? version : 1L, 
                                                   articleId);
            log.debug("Synchronized vector store metadata for article ID {}: {} chunks updated.", articleId, updated);
        } catch (Exception ex) {
            log.warn("Failed to synchronize vector metadata for article ID {}: {}", articleId, ex.getMessage());
        }
    }

    /**
     * Deletes all vector chunks associated with an article ID from PGVector.
     */
    @Transactional
    public void deleteVectorChunks(Long articleId) {
        if (articleId == null) return;
        try {
            int deleted = jdbcTemplate.update("DELETE FROM vector_store WHERE (metadata->>'articleId')::bigint = ?", articleId);
            log.info("Deleted {} vector chunks for article ID {} from vector store.", deleted, articleId);
        } catch (Exception ex) {
            log.warn("Failed to delete vector chunks for article ID {}: {}", articleId, ex.getMessage());
        }
    }

    /**
     * Bulk synchronizes all vector store chunks currently in DRAFT status to PUBLISHED status.
     */
    @Transactional
    public void syncBulkPublishMetadata() {
        String sql = """
            UPDATE vector_store
            SET metadata = (metadata::jsonb || jsonb_build_object('status', 'PUBLISHED'))::json
            WHERE metadata->>'status' = 'DRAFT'
            """;
        try {
            int updated = jdbcTemplate.update(sql);
            log.info("Bulk synchronized vector store status to PUBLISHED for {} chunks.", updated);
        } catch (Exception ex) {
            log.warn("Failed to bulk synchronize vector store status: {}", ex.getMessage());
        }
    }
}