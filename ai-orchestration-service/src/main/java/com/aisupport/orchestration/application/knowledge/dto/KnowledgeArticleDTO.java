package com.aisupport.orchestration.application.knowledge.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticleDTO {
    private Long id;
    private String title;
    private String content;
    private EmbeddingStatus embeddingStatus;
    private Long accessCount;
    private KnowledgeArticleStatus status;
    private String category;
    private List<String> tags;
    private Instant lastAccessedAt;
    private String authorId;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
