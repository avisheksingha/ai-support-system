package com.aisupport.orchestration.infrastructure.web;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.knowledge.KnowledgeBaseService;
import com.aisupport.orchestration.application.knowledge.dto.ArticleSearchRequestDTO;
import com.aisupport.orchestration.application.knowledge.dto.KnowledgeArticleDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/knowledge-base")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Knowledge Base", description = "Frontend API for the Knowledge Base Dashboard")
public class KnowledgeBaseController {

    private final KnowledgeBaseService service;

    @PostMapping("/search")
    @Operation(summary = "Search knowledge base", description = "Search knowledge base articles with filters and pagination")
    public ResponseEntity<Page<KnowledgeArticleDTO>> searchArticles(@RequestBody ArticleSearchRequestDTO request) {
        log.info("API request to search knowledge base");
        return ResponseEntity.ok(service.searchArticles(request));
    }

    @GetMapping("/articles/{id}")
    @Operation(summary = "Get knowledge article", description = "Get a knowledge article by ID")
    public ResponseEntity<KnowledgeArticleDTO> getArticleById(@PathVariable Long id) {
        log.info("API request to get knowledge article {}", id);
        KnowledgeArticleDTO dto = service.getArticleById(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/articles")
    @Operation(summary = "Create a knowledge article", description = "Create a new knowledge article in the Knowledge Base")
    public ResponseEntity<KnowledgeArticleDTO> createArticle(@RequestBody KnowledgeArticleDTO dto) {
        log.info("API request to create knowledge article");
        return ResponseEntity.ok(service.createArticle(dto));
    }

    @PutMapping("/articles/{id}")
    @Operation(summary = "Update a knowledge article", description = "Update an existing knowledge article")
    public ResponseEntity<KnowledgeArticleDTO> updateArticle(@PathVariable Long id, @RequestBody KnowledgeArticleDTO dto) {
        log.info("API request to update knowledge article {}", id);
        return ResponseEntity.ok(service.updateArticle(id, dto));
    }

    @DeleteMapping("/articles/{id}")
    @Operation(summary = "Delete a knowledge article", description = "Delete an existing knowledge article")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        log.info("API request to delete knowledge article {}", id);
        service.deleteArticle(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/articles/sync-embeddings")
    @Operation(summary = "Trigger embeddings generation", description = "Manually trigger embedding generation for pending articles")
    public ResponseEntity<Map<String, Integer>> syncEmbeddings() {
        log.info("API request to sync embeddings");
        int count = service.syncEmbeddings();
        return ResponseEntity.ok(Map.of("embeddedCount", count));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get aggregate knowledge base stats", description = "Get overall article counts across the entire knowledge base")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("API request to get knowledge base stats");
        return ResponseEntity.ok(service.getStats());
    }

    @PostMapping("/articles/bulk-publish")
    @Operation(summary = "Bulk publish draft articles", description = "Publishes all articles currently in DRAFT status. ADMIN only.")
    public ResponseEntity<Map<String, Object>> bulkPublishDraftArticles() {
        log.info("API request to bulk publish draft articles");
        return ResponseEntity.ok(service.bulkPublishDraftArticles());
    }
}
