package com.aisupport.rag.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.rag.dto.ArticleSearchRequestDTO;
import com.aisupport.rag.dto.KnowledgeArticleDTO;
import com.aisupport.rag.service.KnowledgeArticleService;
import com.aisupport.rag.service.KnowledgeEmbeddingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/internal/rag/articles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Knowledge Articles", description = "Internal APIs for Knowledge Base Management")
public class KnowledgeArticleController {

    private final KnowledgeArticleService articleService;
    private final KnowledgeEmbeddingService embeddingService;

    @PostMapping("/search")
    @Operation(summary = "Search knowledge articles", description = "Search articles with pagination, sorting, and filters")
    public ResponseEntity<Page<KnowledgeArticleDTO>> searchArticles(@RequestBody ArticleSearchRequestDTO request) {
        log.info("REST request to search articles");
        return ResponseEntity.ok(articleService.searchArticles(request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a knowledge article by ID")
    public ResponseEntity<KnowledgeArticleDTO> getArticleById(@PathVariable Long id) {
        log.info("REST request to get article: {}", id);
        KnowledgeArticleDTO article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    @PostMapping
    @Operation(summary = "Create a new knowledge article")
    public ResponseEntity<KnowledgeArticleDTO> createArticle(@RequestBody KnowledgeArticleDTO dto) {
        return ResponseEntity.ok(articleService.createArticle(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing knowledge article")
    public ResponseEntity<KnowledgeArticleDTO> updateArticle(@PathVariable Long id, @RequestBody KnowledgeArticleDTO dto) {
        return ResponseEntity.ok(articleService.updateArticle(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a knowledge article")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync-embeddings")
    @Operation(summary = "Manually trigger embedding generation for pending articles")
    public ResponseEntity<Map<String, Integer>> syncEmbeddings() {
        int count = embeddingService.embedPendingArticles();
        return ResponseEntity.ok(Map.of("embeddedCount", count));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get aggregate statistics for knowledge articles")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(articleService.getArticleStats());
    }

    @PostMapping("/bulk-publish")
    @Operation(summary = "Bulk publish all draft articles", description = "Publishes all articles currently in DRAFT status")
    public ResponseEntity<Map<String, Object>> bulkPublishDraftArticles() {
        log.info("REST request to bulk publish draft articles");
        return ResponseEntity.ok(articleService.bulkPublishDraftArticles());
    }
}
