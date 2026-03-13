package com.aisupport.rag.config;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
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

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(3).build())
                .build();
    }
}