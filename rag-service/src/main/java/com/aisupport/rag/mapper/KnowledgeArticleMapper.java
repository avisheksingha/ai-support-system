package com.aisupport.rag.mapper;

import org.mapstruct.Mapper;

import com.aisupport.rag.dto.KnowledgeArticleDTO;
import com.aisupport.rag.entity.KnowledgeArticle;

@Mapper(componentModel = "spring")
public interface KnowledgeArticleMapper {
    KnowledgeArticleDTO toDto(KnowledgeArticle entity);
    KnowledgeArticle toEntity(KnowledgeArticleDTO dto);
}
