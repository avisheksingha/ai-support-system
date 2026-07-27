package com.aisupport.rag.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
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
import com.aisupport.rag.repository.KnowledgeArticleRepository;
import com.aisupport.rag.repository.RagResponseRepository;
import com.aisupport.rag.service.retrieval.MetadataAwareRetrievalService;
import com.aisupport.rag.service.retrieval.RetrievalResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {
	
	private static final String NO_KNOWLEDGE_FOUND = "No relevant knowledge article found.";

	private final ChatClient chatClient;

	private final MetadataAwareRetrievalService retrievalService;
	private final RagResponseRepository ragResponseRepository;
	private final KnowledgeArticleRepository knowledgeArticleRepository;
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
		int docCount = 0;
		String titles = null;
		String sourceDetails = null;
		
		String systemPrompt = ragSystemPromptTemplate.render(
		        Map.of("noKnowledgeFound", NO_KNOWLEDGE_FOUND));
		
		// Google GenAI call — can fail due to network/quota/model issues
		try {
			RetrievalResult retrievalResult = retrievalService.retrieveAndRank(query);
			String fullSystemPrompt = systemPrompt + "\n\nRetrieved Knowledge Base Context:\n" +
			        (retrievalResult.formattedContext().isEmpty() ? NO_KNOWLEDGE_FOUND : retrievalResult.formattedContext());
			
			// RAG call — similarity search + Google GenAI generation
	        response = chatClient.prompt()
				.system(fullSystemPrompt)
	            .user(query)
	            .call()
	            .content();
	        docCount = retrievalResult.retrievedDocumentCount();
	        titles = retrievalResult.matchedArticleTitles();
	        sourceDetails = serializeSources(retrievalResult.documents());
	        if (docCount > 0 && titles != null && !titles.isEmpty()) {
	            incrementAccessCountSafe(List.of(titles.split(",")));
	        }
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
                .retrievedDocumentCount(docCount)
                .matchedArticleTitles(titles)
                .sourceDetails(sourceDetails)
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
	    String sourceDetails = null;
	    String systemPrompt = ragSystemPromptTemplate.render(
	            Map.of("noKnowledgeFound", NO_KNOWLEDGE_FOUND));
	    
	    try {
	        RetrievalResult retrievalResult = retrievalService.retrieveAndRank(query);
	        String fullSystemPrompt = systemPrompt + "\n\nRetrieved Knowledge Base Context:\n" +
	                (retrievalResult.formattedContext().isEmpty() ? NO_KNOWLEDGE_FOUND : retrievalResult.formattedContext());

	        ChatResponse chatResponse = chatClient.prompt()
	            .system(fullSystemPrompt)
	            .user(query)
	            .call()
	            .chatResponse();
	            
	        if (chatResponse == null || chatResponse.getResult() == null || chatResponse.getResult().getOutput() == null) {
	            // Throwing IllegalStateException allows the catch block to handle it properly
	            throw new IllegalStateException("Empty or null chat response received for ticketId: " + ticketId);
	        }

	        response = chatResponse.getResult().getOutput().getText();
	        docCount = retrievalResult.retrievedDocumentCount();
	        titles = retrievalResult.matchedArticleTitles();
	        sourceDetails = serializeSources(retrievalResult.documents());
	        
	        if (docCount > 0 && titles != null && !titles.isEmpty()) {
	            incrementAccessCountSafe(List.of(titles.split(",")));
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
	            .sourceDetails(sourceDetails)
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

	private void incrementAccessCountSafe(List<String> matchedTitles) {
		try {
			knowledgeArticleRepository.incrementAccessCountByTitles(matchedTitles);
		} catch (Exception e) {
			log.warn("Failed to increment access count for titles: {}", matchedTitles, e);
		}
	}

	private String serializeSources(List<Document> documents) {
	    if (documents == null || documents.isEmpty()) {
	        return null;
	    }
	    try {
	        List<Map<String, Object>> list = documents.stream().map(d -> {
	            Map<String, Object> map = new HashMap<>();
	            map.put("id", String.valueOf(d.getMetadata().getOrDefault("articleId", d.getId())));
	            map.put("title", String.valueOf(d.getMetadata().getOrDefault("title", "Unknown")));
	            map.put("category", String.valueOf(d.getMetadata().getOrDefault("category", "General")));
	            map.put("tags", String.valueOf(d.getMetadata().getOrDefault("tags", "None")));
	            double fallbackScore = d.getScore() != null ? d.getScore() : 0.0;
	            map.put("similarityScore", fallbackScore);
	            map.put("vectorScore", d.getMetadata().get("vectorScore") instanceof Number n ? n.doubleValue() : fallbackScore);
	            map.put("hybridScore", d.getMetadata().get("hybridScore") instanceof Number n ? n.doubleValue() : fallbackScore);
	            return map;
	        }).toList();
	        return new ObjectMapper().writeValueAsString(list);
	    } catch (Exception e) {
	        log.warn("Failed to serialize sources to JSON", e);
	        return null;
	    }
	}
}
