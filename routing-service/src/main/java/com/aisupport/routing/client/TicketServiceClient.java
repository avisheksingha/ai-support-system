package com.aisupport.routing.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.aisupport.common.dto.TicketDTO;
import com.aisupport.routing.exception.ServiceCommunicationException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TicketServiceClient {
    
    private final WebClient webClient;
    
    public TicketServiceClient(WebClient.Builder webClientBuilder,
                              @Value("${api.services.ticket.url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }
    
    public TicketDTO getTicketById(Long ticketId) {
        log.info("Fetching ticket from Ticket Service: {}", ticketId);
        
        try {
            return webClient.get()
                    .uri("/api/v1/tickets/id/{id}", ticketId)
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
    
    public TicketDTO assignTicket(String ticketNumber, String assignedTo, Integer slaHours) {
        log.info("Assigning ticket {} to {} with SLA {}", ticketNumber, assignedTo, slaHours);
        
        try {
            return webClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/tickets/{ticketNumber}/assign")
                            .queryParam("assignedTo", assignedTo)
                            .queryParamIfPresent("slaHours", java.util.Optional.ofNullable(slaHours))
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
    
    public TicketDTO updatePriority(String ticketNumber, String priority, Integer slaHours) {
        log.info("Updating priority for ticket {} to {} with SLA {}", ticketNumber, priority, slaHours);
        
        try {
            return webClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/tickets/{ticketNumber}/priority")
                            .queryParam("priority", priority)
                            .queryParamIfPresent("slaHours", java.util.Optional.ofNullable(slaHours))
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