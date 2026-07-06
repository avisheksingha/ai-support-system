package com.aisupport.rag.runner;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.aisupport.rag.entity.KnowledgeArticle;
import com.aisupport.rag.repository.KnowledgeArticleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads knowledge articles from PostgreSQL into PGVector on application startup.
 *
 * How it works:
 * 1. Reads all KnowledgeArticle records from the database
 * 2. Converts each article into a Spring AI Document (with title + content as text)
 * 3. Adds them to the VectorStore — this automatically:
 *    a. Sends each document's text to the Google GenAI Embedding Model
 *    b. Stores the resulting embedding vector + document text in the PGVector table
 *
 * Once loaded, these embeddings are used by QuestionAnswerAdvisor for similarity search
 * when answering user queries via RAG.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class KnowledgeDataInitializer implements CommandLineRunner {

    private final KnowledgeArticleRepository repo;
    private final VectorStore vectorStore;

    @Override
    public void run(String... args) throws Exception {

            // Skip if already populated
            long count = repo.countEmbeddedArticles();
            if (count > 0) {
                log.info("Vector store already populated with {} articles — skipping load", count);
                return;
            }

            List<KnowledgeArticle> articles = repo.findAll();
            log.info("Loading {} knowledge articles into vector store...", articles.size());

            List<Document> docs = articles.stream()
                    .map(article -> new Document(
                            article.getTitle() + ": " + article.getContent(),
                            Map.of("articleId", article.getId(), "title", article.getTitle())
                    ))
                    .toList();

            vectorStore.add(docs); // embed into PGVector first

            articles.forEach(a -> a.setEmbedded(true));     // mark as embedded
            repo.saveAll(articles);                         // persist the flag

        log.info("Successfully loaded {} articles into vector store.", docs.size());
    }
}

