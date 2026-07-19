package com.aisupport.rag.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.event.EventType;
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
 * 3. Retrieve relevant documents using VectorStore (PGVector)
 * 4. Generate a grounded response using Google GenAI
 * 5. Publish a Kafka event with the response to rag_responses table
 * 6. Publish TicketRagResponseEvent via outbox
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {
	
	private static final String NO_KNOWLEDGE_FOUND = "No relevant knowledge article found.";
	
	private static final String TITLE_KEY = "title";
	private static final String UNKNOWN_TITLE = "Unknown";

	private final ChatClient chatClient;
	private final QuestionAnswerAdvisor questionAnswerAdvisor;
	private final RagResponseRepository ragResponseRepository;
	private final OutboxEventService outboxEventService;
	private final PromptTemplate ragSystemPromptTemplate;
	
	@Value("${spring.ai.google.genai.chat.model}")
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
		
		String systemPrompt = ragSystemPromptTemplate.render(
		        Map.of("noKnowledgeFound", NO_KNOWLEDGE_FOUND));
		
		// Google GenAI call — can fail due to network/quota/model issues
		try {
		
			// RAG call — similarity search + Google GenAI generation
	        response = chatClient.prompt()
				.system(systemPrompt)
	            .user(query)
	            .advisors(questionAnswerAdvisor)
	            .call()
	            .content();
		} catch (Exception e) {
			log.error("Google GenAI RAG generation failed for ticketId={}", ticketId, e);
	        throw new RagGenerationException(
	                "RAG generation failed for ticketId: " + ticketId, e);
	    } 

        // Persist to rag_responses table — inside @Transactional
        RagResponse ragResponse = RagResponse.builder()
                .ticketId(ticketId)
                .query(query)
                .response(response)
                .model(chatModel)
                .knowledgeFound(isKnowledgeFound(response))
                .build();

        ragResponseRepository.save(ragResponse);
        log.info("RAG response persisted for ticketId={}", ticketId);

        // Publish event via outbox so ticket-service can update the ticket — also inside @Transactional
        TicketRagResponseEvent event = TicketRagResponseEvent.builder()
                .ticketId(ticketId)
                .query(query)
                .response(response)
                .model(chatModel)
                .generatedAt(Instant.now())
                .build();

        outboxEventService.publishEvent(
                "TICKET",
                ticketId.toString(),
                EventType.TICKET_RAG_RESPONSE_GENERATED,
                event
        );

        log.info("RAG response event published for ticketId={}", ticketId);

        return response;
	}

	@Transactional
	public RagResponse generateResponseSync(Long ticketId, String query) {
	    log.info("Running sync RAG for query: {}", query);
	    
	    String response;
	    int docCount = 0;
	    String titles = null;
	    String systemPrompt = ragSystemPromptTemplate.render(
	            Map.of("noKnowledgeFound", NO_KNOWLEDGE_FOUND));
	    
	    try {
	        ChatResponse chatResponse = chatClient.prompt()
	            .system(systemPrompt)
	            .user(query)
	            .advisors(questionAnswerAdvisor)
	            .call()
	            .chatResponse();
	            
	        if (chatResponse == null || chatResponse.getResult() == null || chatResponse.getResult().getOutput() == null) {
	            // Throwing IllegalStateException allows the catch block to handle it properly
	            throw new IllegalStateException("Empty or null chat response received for ticketId: " + ticketId);
	        }

	        response = chatResponse.getResult().getOutput().getText();
	        
	        // Safely retrieve and cast the documents
	        List<Document> docs = Collections.emptyList();
	        Object rawDocs = chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
	        
	        if (rawDocs instanceof List<?> rawList) {
	            docs = rawList.stream()
	                    .filter(Document.class::isInstance)
	                    .map(Document.class::cast)
	                    .toList();
	        }
	            
	        if (!docs.isEmpty()) {
	            docCount = docs.size();
	            titles = docs.stream()
	                .map(d -> d.getMetadata().get(TITLE_KEY) != null ? d.getMetadata().get(TITLE_KEY).toString() : UNKNOWN_TITLE)
	                .distinct()
	                .collect(Collectors.joining(","));
	        }
	        
	    } catch (Exception e) {
	        log.error("Google GenAI RAG generation failed for ticketId={}", ticketId, e);
	        throw new RagGenerationException(
	                "RAG generation failed for ticketId: " + ticketId, e);
	    }

	    // If exists, delete old one for idempotency
	    if (ragResponseRepository.existsById(ticketId)) {
	        ragResponseRepository.deleteById(ticketId);
	    }

	    RagResponse ragResponse = RagResponse.builder()
	            .ticketId(ticketId)
	            .query(query)
	            .response(response)
	            .model(chatModel)
	            .knowledgeFound(isKnowledgeFound(response))
	            .retrievedDocumentCount(docCount)
	            .matchedArticleTitles(titles)
	            .build();

	    ragResponseRepository.save(ragResponse);
	    log.info("Sync RAG response persisted for ticketId={}", ticketId);

	    return ragResponse;
	}

	/**
	 * Retrieves the most recent RAG response for a given ticket.
	 */
	public Optional<RagResponse> getRagResponseForTicket(Long ticketId) {
		return ragResponseRepository.findTopByTicketIdOrderByCreatedAtDesc(ticketId);
	}

	/**
	 * Helper method to determine if the response indicates that relevant knowledge was found.
	 * This is based on whether the response matches the NO_KNOWLEDGE_FOUND message.
	 */
	private boolean isKnowledgeFound(String response) {
		return response != null
			&& !NO_KNOWLEDGE_FOUND.equalsIgnoreCase(response.trim());
	}
}
