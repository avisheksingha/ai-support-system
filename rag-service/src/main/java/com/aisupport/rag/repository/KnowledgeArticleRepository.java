package com.aisupport.rag.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aisupport.rag.entity.KnowledgeArticle;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    // Faster than counting all and subtracting
    long countByEmbeddedFalse();

    // Only fetch what we need
    List<KnowledgeArticle> findByEmbeddedFalse();

    // Single SQL UPDATE statement: UPDATE knowledge_article SET embedded = true WHERE id IN (...)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE KnowledgeArticle a SET a.embedded = true WHERE a.id IN :ids")
    void markArticlesAsEmbedded(@Param("ids") List<Long> ids);
}
