package com.aisupport.rag.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aisupport.rag.embedding.EmbeddingProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingProvider embeddingProvider;

    public List<Float> generateEmbedding(String text) {
        return embeddingProvider.embed(text);
    }
}
