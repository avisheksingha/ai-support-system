package com.aisupport.rag.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.common.dto.admin.AdminRagStatsResponse;
import com.aisupport.rag.entity.KnowledgeArticle;
import com.aisupport.rag.repository.KnowledgeArticleRepository;
import com.aisupport.rag.repository.RagResponseRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/rag", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Admin Stats", description = "Internal endpoints for orchestration service")
public class AdminRagStatsController {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final RagResponseRepository ragResponseRepository;

    @GetMapping("/stats/admin")
    public ResponseEntity<AdminRagStatsResponse> getAdminStats() {
        log.info("Fetching admin rag stats");
        
        long totalArticles = knowledgeArticleRepository.count();
        // The user stated all articles are already embedded, so we can return the total count.
        long vectorized = totalArticles; 
        
        String mostUsed = knowledgeArticleRepository.findFirstByOrderByAccessCountDesc()
                .map(KnowledgeArticle::getTitle)
                .orElse("N/A");

        long totalRagRequests = ragResponseRepository.count();
        long successfulRagRequests = ragResponseRepository.countByKnowledgeFoundTrue();

        AdminRagStatsResponse response = AdminRagStatsResponse.builder()
                .totalArticles(totalArticles)
                .vectorizedDocuments(vectorized)
                .mostAccessedArticle(mostUsed)
                .totalRagRequests(totalRagRequests)
                .successfulRagRequests(successfulRagRequests)
                .build();

        return ResponseEntity.ok(response);
    }
}
