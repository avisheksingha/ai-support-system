package com.aisupport.routing.client;

import com.aisupport.common.dto.TicketDTO;
import com.aisupport.routing.exception.ServiceCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class TicketServiceClient {
    
    private final WebClient webClient;
    private final String baseUrl;
    
    public TicketServiceClient(WebClient.Builder webClientBuilder,
                              @Value("${webclient.service.ticket-service}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }
    
    public TicketDTO getTicketById(Long ticketId) {
        log.info("Fetching ticket from Ticket Service: {}", ticketId);
        
        try {
            return webClient.get()
                    .uri("/api/v1/tickets/{id}", ticketId)
                    .retrieve()
                    .bodyToMono(TicketDTO.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(ticket -> log.debug("Successfully fetched ticket: {}", ticketId))
                    .doOnError(error -> log.error("Error fetching ticket {}: {}", ticketId, error.getMessage()))
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to fetch ticket {}", ticketId, e);
            throw new ServiceCommunicationException(
                    "Failed to communicate with Ticket Service", e);
        }
    }
    
    public TicketDTO assignTicket(String ticketNumber, String assignedTo) {
        log.info("Assigning ticket {} to {}", ticketNumber, assignedTo);
        
        try {
            return webClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/tickets/{ticketNumber}/assign")
                            .queryParam("assignedTo", assignedTo)
                            .build(ticketNumber))
                    .retrieve()
                    .bodyToMono(TicketDTO.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(ticket -> log.debug("Successfully assigned ticket: {}", ticketNumber))
                    .doOnError(error -> log.error("Error assigning ticket {}: {}", ticketNumber, error.getMessage()))
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to assign ticket {}", ticketNumber, e);
            throw new ServiceCommunicationException(
                    "Failed to assign ticket", e);
        }
    }
    
    public TicketDTO updatePriority(String ticketNumber, String priority) {
        log.info("Updating priority for ticket {} to {}", ticketNumber, priority);
        
        try {
            return webClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/tickets/{ticketNumber}/status")
                            .queryParam("status", "ASSIGNED")
                            .build(ticketNumber))
                    .retrieve()
                    .bodyToMono(TicketDTO.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(ticket -> log.debug("Successfully updated priority: {}", ticketNumber))
                    .doOnError(error -> log.error("Error updating priority {}: {}", ticketNumber, error.getMessage()))
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to update priority for ticket {}", ticketNumber, e);
            throw new ServiceCommunicationException(
                    "Failed to update ticket priority", e);
        }
    }
}