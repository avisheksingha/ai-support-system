package com.aisupport.rag.consumer;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aisupport.common.constant.Correlation;
import com.aisupport.common.constant.HttpHeaders;
import com.aisupport.common.constant.KafkaGroups;
import com.aisupport.common.constant.KafkaTopics;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.exception.TicketEventProcessingException;
import com.aisupport.rag.service.RagService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAnalyzedConsumer {

    private final RagService ragService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.TICKET_ANALYZED, groupId = KafkaGroups.RAG)
    public void consume(ConsumerRecord<String, String> consumerRecord) {
    	
    	// Extract correlationId from Kafka header into MDC
    	Header correlationHeader = consumerRecord.headers().lastHeader(HttpHeaders.CORRELATION_ID);
        if (correlationHeader != null) {
            MDC.put(Correlation.MDC_KEY, new String(correlationHeader.value(), StandardCharsets.UTF_8));
        }
    
    	try {
        	
        	String payload = consumerRecord.value(); // extract payload

	        TicketAnalyzedEvent event = objectMapper.readValue(payload, TicketAnalyzedEvent.class);
	
	        log.info("Consumed ticket-analyzed event: ticketId={}", event.getTicketId());
	
	        String query = String.join(" ",
	                safe(event.getIntent()),
	                safe(event.getSentiment()),
	                safe(event.getUrgency()),
	                String.join(" ", safeList(event.getKeywords()))
	        );
	        
	        log.debug("Constructed RAG query: {}", query);
	
	        String response = ragService.generateResponse(query);
	
	        log.info("Generated RAG response for ticketId={}: {}", event.getTicketId(), response);

	    } catch (Exception ex) {	    	
	        log.error("Failed to process ticket-analyzed event key={}", consumerRecord.key(), ex);	        
	        throw new TicketEventProcessingException("Error processing ticket-analyzed event", ex);
	    } finally {
        	MDC.remove(Correlation.MDC_KEY);
        }
	}

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private List<String> safeList(List<String> list) {
        return list == null ? List.of() : list;
    }
}