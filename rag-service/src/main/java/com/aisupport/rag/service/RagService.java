package com.aisupport.rag.service;

import java.time.LocalDateTime;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.event.TicketRagResponseEvent;
import com.aisupport.rag.entity.RagResponse;
import com.aisupport.rag.exception.RagGenerationException;
import com.aisupport.rag.outbox.OutboxEventService;
import com.aisupport.rag.repository.RagResponseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core RAG (Retrieval-Augmented Generation) service.
 *
 * This service uses Spring AI's ChatClient with a QuestionAnswerAdvisor to:
 * 1. Accept a natural-language query (built from ticket analysis fields)
 * 2. Retrieve relevant knowledge articles from PGVector via similarity search
 * 3. Augment the prompt with the retrieved context
 * 4. Generate a grounded response using Gemini
 * 5. Persist the response to rag_responses table
 * 6. Publish TicketRagResponseEvent via outbox
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

	private final ChatClient chatClient;
	private final QuestionAnswerAdvisor questionAnswerAdvisor;
	private final RagResponseRepository ragResponseRepository;
	private final OutboxEventService outboxEventService;
	
	@Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
	private String chatModel;

	/**
	 * Generate a context-aware response for the given query using RAG.
	 *
	 * @param ticketId ticket identifier associated with the query
	 * @param query the search query (typically built from ticket analysis fields)
	 * @return the AI-generated response grounded in knowledge base context
	 */
	@Transactional
	public String generateResponse(Long ticketId, String query) {

		log.info("Running RAG for query: {}", query);
		
		String response;
		
		// Gemini call — can fail due to network/quota/model issues
		try {
		
			// RAG call — similarity search + Gemini generation
	        response = chatClient.prompt()
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
	        log.error("Gemini RAG generation failed for ticketId={} query={}",
	                ticketId, query, e);
	        throw new RagGenerationException(
	                "RAG generation failed for ticketId: " + ticketId, e);
	    } 

        // Persist to rag_responses table — inside @Transactional
        RagResponse ragResponse = RagResponse.builder()
                .ticketId(ticketId)
                .query(query)
                .response(response)
                .model(chatModel)
                .build();

        ragResponseRepository.save(ragResponse);
        log.info("RAG response persisted for ticketId={}", ticketId);

        // Publish event via outbox so ticket-service can update the ticket — also inside @Transactional
        TicketRagResponseEvent event = TicketRagResponseEvent.builder()
                .ticketId(ticketId)
                .query(query)
                .response(response)
                .model(chatModel)
                .generatedAt(LocalDateTime.now())
                .build();

        outboxEventService.publishEvent(
                "TICKET",
                ticketId.toString(),
                "TicketRagResponseEvent",
                event
        );

        log.info("RAG response event published for ticketId={}", ticketId);

        return response;
	}
}
