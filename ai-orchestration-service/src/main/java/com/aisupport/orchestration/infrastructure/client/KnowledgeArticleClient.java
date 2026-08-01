package com.aisupport.orchestration.infrastructure.client;

import java.util.Map;

import org.springframework.data.domain.Page;

import com.aisupport.orchestration.application.knowledge.dto.ArticleSearchRequestDTO;
import com.aisupport.orchestration.application.knowledge.dto.KnowledgeArticleDTO;

public interface KnowledgeArticleClient {
    Page<KnowledgeArticleDTO> searchArticles(ArticleSearchRequestDTO request);
    KnowledgeArticleDTO getArticleById(Long id);
    KnowledgeArticleDTO createArticle(KnowledgeArticleDTO dto);
    KnowledgeArticleDTO updateArticle(Long id, KnowledgeArticleDTO dto);
    void deleteArticle(Long id);
    int syncEmbeddings();
    Map<String, Object> getStats();
    Map<String, Object> bulkPublishDraftArticles();
}
