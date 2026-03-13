package com.aisupport.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core RAG (Retrieval-Augmented Generation) service.
 *
 * This service uses Spring AI's ChatClient with a QuestionAnswerAdvisor to:
 * 1. Accept a natural-language query (e.g., built from ticket analysis fields)
 * 2. Retrieve relevant knowledge articles from PGVector via similarity search
 * 3. Augment the prompt with the retrieved context
 * 4. Generate a grounded response using Gemini
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

	private final ChatClient chatClient;
	private final QuestionAnswerAdvisor questionAnswerAdvisor;

	/**
	 * Generate a context-aware response for the given query using RAG.
	 *
	 * @param query the search query (typically built from ticket analysis fields)
	 * @return the AI-generated response grounded in knowledge base context
	 */
	public String generateResponse(String query) {

		log.info("Running RAG for query: {}", query);

		try {
			return chatClient.prompt()
					.system("""
						Support assistant. Use ONLY the provided context.
						If not found: "No relevant knowledge article found."
						Max 3 sentences. Direct and practical.
						""")
					.user(query)
					.advisors(questionAnswerAdvisor)
					.call()
					.content();
		} catch (Exception e) {
			log.error("RAG generation failed for query: {}", query, e);
			return "Unable to generate response at this time.";
		}	        
	}
}