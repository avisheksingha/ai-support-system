package com.aisupport.orchestration.infrastructure.client;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.orchestration.application.knowledge.dto.ArticleSearchRequestDTO;
import com.aisupport.orchestration.application.knowledge.dto.KnowledgeArticleDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultKnowledgeArticleClient implements KnowledgeArticleClient {

    private final RestClient restClient;

    public DefaultKnowledgeArticleClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                                         @Value("${api.services.rag.url:http://RAG-SERVICE}") String ragServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(ragServiceUrl).build();
    }

    @Override
    public Page<KnowledgeArticleDTO> searchArticles(ArticleSearchRequestDTO request) {
        log.info("Calling rag-service to search knowledge articles");
        RestResponsePage<KnowledgeArticleDTO> pageResponse = restClient.post()
                .uri("/api/internal/rag/articles/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<RestResponsePage<KnowledgeArticleDTO>>() {});
        return pageResponse != null ? pageResponse.toPage() : new PageImpl<>(Collections.emptyList());
    }

    @Override
    public KnowledgeArticleDTO getArticleById(Long id) {
        log.info("Calling rag-service to get article {}", id);
        return restClient.get()
                .uri("/api/internal/rag/articles/" + id)
                .retrieve()
                .body(KnowledgeArticleDTO.class);
    }

    @Override
    public KnowledgeArticleDTO createArticle(KnowledgeArticleDTO dto) {
        return restClient.post()
                .uri("/api/internal/rag/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(KnowledgeArticleDTO.class);
    }

    @Override
    public KnowledgeArticleDTO updateArticle(Long id, KnowledgeArticleDTO dto) {
        return restClient.put()
                .uri("/api/internal/rag/articles/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(KnowledgeArticleDTO.class);
    }

    @Override
    public void deleteArticle(Long id) {
        restClient.delete()
                .uri("/api/internal/rag/articles/" + id)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public int syncEmbeddings() {
        return restClient.post()
                .uri("/api/internal/rag/articles/sync-embeddings")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Integer>>() {})
                .get("embeddedCount");
    }

    @Override
    public Map<String, Object> getStats() {
        return restClient.get()
                .uri("/api/internal/rag/articles/stats")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    @Override
    public Map<String, Object> bulkPublishDraftArticles() {
        return restClient.post()
                .uri("/api/internal/rag/articles/bulk-publish")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
