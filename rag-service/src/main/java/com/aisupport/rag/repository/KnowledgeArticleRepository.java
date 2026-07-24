package com.aisupport.rag.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aisupport.rag.entity.EmbeddingStatus;
import com.aisupport.rag.entity.KnowledgeArticle;
import com.aisupport.rag.entity.KnowledgeArticleStatus;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long>, JpaSpecificationExecutor<KnowledgeArticle> {

    long countByStatus(KnowledgeArticleStatus status);

    @Query("SELECT COUNT(DISTINCT a.category) FROM KnowledgeArticle a WHERE a.category IS NOT NULL AND a.category <> '' AND a.category <> '-'")
    long countDistinctCategories();

    // Faster than counting all and subtracting
    long countByEmbeddingStatus(EmbeddingStatus status);

    // Only fetch what we need
    List<KnowledgeArticle> findByEmbeddingStatus(EmbeddingStatus status);

    // Single SQL UPDATE statement
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE KnowledgeArticle a SET a.embeddingStatus = 'READY' WHERE a.id IN :ids")
    void markArticlesAsEmbedded(@Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE KnowledgeArticle a SET a.embeddingStatus = 'READY'")
    void markAllAsEmbedded();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE KnowledgeArticle a SET a.embeddingStatus = :status WHERE a.id IN :ids")
    void updateEmbeddingStatus(@Param("ids") List<Long> ids, @Param("status") EmbeddingStatus status);

    Optional<KnowledgeArticle> findFirstByOrderByAccessCountDesc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE KnowledgeArticle a SET a.accessCount = a.accessCount + 1, a.lastAccessedAt = CURRENT_TIMESTAMP WHERE a.title IN :titles")
    void incrementAccessCountByTitles(@Param("titles") List<String> titles);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE KnowledgeArticle a SET a.status = 'PUBLISHED' WHERE a.status = 'DRAFT'")
    int bulkPublishDraftArticles();
}
