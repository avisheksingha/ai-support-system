package com.aisupport.rag.dto;

import java.util.List;

import com.aisupport.rag.entity.KnowledgeArticleStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSearchRequestDTO {
    private String query;
    private KnowledgeArticleStatus status;
    private String category;
    private List<String> tags;
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
    
    @Builder.Default
    private String sortBy = "accessCount";
    
    @Builder.Default
    private String sortDirection = "desc";
}
