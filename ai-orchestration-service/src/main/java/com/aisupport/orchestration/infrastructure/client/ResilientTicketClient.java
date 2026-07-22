package com.aisupport.orchestration.infrastructure.client;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.infrastructure.client.dto.TicketDashboardSummaryDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientTicketClient {

    private final TicketClient ticketClient;

    @CircuitBreaker(name = "ticketService", fallbackMethod = "fallbackAgentSummary")
    @Retry(name = "ticketService")
    public TicketDashboardSummaryDTO getAgentSummary(String userEmail) {
        log.info("Fetching agent dashboard summary from ticket-service for {}", userEmail);
        return ticketClient.getAgentSummary(userEmail);
    }

    public TicketDashboardSummaryDTO fallbackAgentSummary(String userEmail, Throwable t) {
        log.error("Ticket service unavailable. Returning empty agent dashboard summary. Error: {}", t.getMessage());
        return new TicketDashboardSummaryDTO();
    }
}
