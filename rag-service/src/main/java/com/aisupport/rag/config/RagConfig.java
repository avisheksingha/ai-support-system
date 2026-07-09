package com.aisupport.rag.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the RAG (Retrieval-Augmented Generation) advisor.
 *
 * QuestionAnswerAdvisor is the core Spring AI component that enables RAG:
 * 1. When a user query arrives, it searches the VectorStore for similar documents
 * 2. Retrieves the top-K most relevant knowledge articles
 * 3. Appends them as context to the user's prompt
 * 4. The LLM then generates a response grounded in this context
 */
@Configuration
public class RagConfig {

    /**
     * Configures the QuestionAnswerAdvisor with a VectorStore and search parameters.
     * The advisor will retrieve the top 5 most similar documents with a similarity threshold of 0.72.
     *
     * @param vectorStore The VectorStore bean that contains embedded knowledge articles
     * @return A configured QuestionAnswerAdvisor bean
     */
    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(
                SearchRequest.builder()
                    .topK(5)
                    .similarityThreshold(0.72)
                    .build()
            )
            .build();
    }

    /**
     * Fallback TextSplitter guaranteed to compile on Spring AI 2.0.0+.
     * Splits documents by sentences to avoid breaking words mid-way, while preserving metadata.
     */
    @Bean
    TextSplitter textSplitter() {
        return new TextSplitter() {

            @Override
            public List<Document> apply(List<Document> documents) {
                List<Document> chunks = new ArrayList<>();
                for (Document doc : documents) {
                    chunks.addAll(createChunks(doc));
                }
                return chunks;
            }

            /**
             * Splits a single document into sentence-based chunks of ~500 characters.
             * Handles null/blank text explicitly to satisfy null-safety analysis (SonarLint S2259)
             * and avoid any risk of NullPointerException downstream.
             */
            private List<Document> createChunks(Document doc) {
                String text = doc.getText();

                if (text == null || text.isBlank()) {
                    return List.of(doc);
                }

                if (text.length() <= 500) {
                    return List.of(doc);
                }

                List<Document> result = new ArrayList<>();
                String[] sentences = text.split("(?<=\\.)\\s+");
                StringBuilder currentChunk = new StringBuilder();

                for (String sentence : sentences) {
                    currentChunk.append(sentence).append(" ");
                    if (currentChunk.length() >= 500) {
                        result.add(new Document(currentChunk.toString().trim(), doc.getMetadata()));
                        currentChunk = new StringBuilder();
                    }
                }

                if (!currentChunk.isEmpty()) {
                    result.add(new Document(currentChunk.toString().trim(), doc.getMetadata()));
                }
                return result;
            }

            @Override
            protected List<String> splitText(String text) {
                return List.of();
            }
        };
    }
    
    /**
	 * Configures a TokenTextSplitter for chunking large documents into smaller pieces.
	 * This is crucial for RAG, as it allows the system to embed and retrieve relevant sections of text.
	 *
	 * @return A configured TokenTextSplitter bean
	 */
    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder()
            .withChunkSize(500)
            .withMinChunkSizeChars(350)
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(10000)
            .withKeepSeparator(true)
            .build();
    }
}