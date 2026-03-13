package com.aisupport.rag.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aisupport.rag.entity.KnowledgeArticle;
import com.aisupport.rag.repository.KnowledgeArticleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final KnowledgeArticleRepository repo;

    public List<KnowledgeArticle> retrieveRelevant(String ticketText) {
        // TEMP: return top 3 (we replace with vector search next)
        return repo.findAll().stream().limit(3).toList();
    }
}
