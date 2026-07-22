package com.aisupport.orchestration.infrastructure.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.orchestration.infrastructure.client.dto.TicketDashboardSummaryDTO;

@Service
public class TicketClient {

    private final RestClient restClient;

    public TicketClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                        @Value("${api.services.ticket.url:http://TICKET-SERVICE}") String ticketServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(ticketServiceUrl)
                .build();
    }

    public TicketDashboardSummaryDTO getAgentSummary(String userEmail) {
        return restClient.get()
                .uri("/api/v1/tickets/summary/agent")
                .header("X-User-Email", userEmail)
                .retrieve()
                .body(TicketDashboardSummaryDTO.class);
    }

}
