package com.aisupport.orchestration.application.knowledge;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.aisupport.orchestration.application.knowledge.dto.ArticleSearchRequestDTO;
import com.aisupport.orchestration.application.knowledge.dto.KnowledgeArticleDTO;
import com.aisupport.orchestration.infrastructure.client.KnowledgeArticleClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {

    private final KnowledgeArticleClient client;

    public Page<KnowledgeArticleDTO> searchArticles(ArticleSearchRequestDTO request) {
        log.info("Proxying search knowledge articles request to rag-service");
        return client.searchArticles(request);
    }

    public KnowledgeArticleDTO getArticleById(Long id) {
        log.info("Proxying get knowledge article {} request to rag-service", id);
        return client.getArticleById(id);
    }

    public KnowledgeArticleDTO createArticle(KnowledgeArticleDTO dto) {
        log.info("Proxying create knowledge article request to rag-service");
        return client.createArticle(dto);
    }

    public KnowledgeArticleDTO updateArticle(Long id, KnowledgeArticleDTO dto) {
        log.info("Proxying update knowledge article {} request to rag-service", id);
        return client.updateArticle(id, dto);
    }

    public void deleteArticle(Long id) {
        log.info("Proxying delete knowledge article {} request to rag-service", id);
        client.deleteArticle(id);
    }

    public int syncEmbeddings() {
        log.info("Proxying sync embeddings request to rag-service");
        return client.syncEmbeddings();
    }

    public Map<String, Object> getStats() {
        log.info("Proxying get knowledge base stats request to rag-service");
        return client.getStats();
    }

    public Map<String, Object> bulkPublishDraftArticles() {
        log.info("Proxying bulk publish draft articles request to rag-service");
        return client.bulkPublishDraftArticles();
    }
}
