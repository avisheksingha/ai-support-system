package com.aisupport.rag.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.aisupport.rag.service.KnowledgeEmbeddingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Local-dev startup trigger for the knowledge embedding pipeline.
 * Delegates all logic to KnowledgeEmbeddingService — this class only
 * decides WHEN to run (on local app startup), not WHAT to run.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class KnowledgeDataInitializer implements CommandLineRunner {

    private final KnowledgeEmbeddingService embeddingService;

    @Override
    public void run(String... args) {
        int embedded = embeddingService.embedPendingArticles();
        if (embedded == 0) {
            log.info("Vector store is already up to date — skipping load");
        } else {
            log.info("Successfully embedded and marked {} articles.", embedded);
        }
    }
}