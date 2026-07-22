package com.aisupport.orchestration.infrastructure.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.orchestration.infrastructure.client.dto.TicketDashboardSummaryDTO;
import com.aisupport.orchestration.infrastructure.client.dto.TicketMessageResponseDTO;
import com.aisupport.orchestration.infrastructure.client.dto.TicketResponseDTO;

@Service
public class TicketClient {

    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    private final RestClient restClient;

    public TicketClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                        @Value("${api.services.ticket.url:http://TICKET-SERVICE}") String ticketServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(ticketServiceUrl)
                .build();
    }

    public TicketDashboardSummaryDTO getAgentSummary(String userEmail, String team) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/tickets/summary/agent")
                        .queryParam("team", team)
                        .build())
                .headers(headers -> addSecurityHeaders(headers, userEmail))
                .retrieve()
                .body(TicketDashboardSummaryDTO.class);
    }

    public TicketResponseDTO[] getCustomerTickets(String userEmail) {
        return restClient.get()
                .uri("/api/v1/tickets/my")
                .headers(headers -> addSecurityHeaders(headers, userEmail))
                .retrieve()
                .body(TicketResponseDTO[].class);
    }

    public TicketResponseDTO getCustomerTicket(String userEmail, String ticketNumber) {
        return restClient.get()
                .uri("/api/v1/tickets/my/{ticketNumber}", ticketNumber)
                .headers(headers -> addSecurityHeaders(headers, userEmail))
                .retrieve()
                .body(TicketResponseDTO.class);
    }

    public TicketMessageResponseDTO[] getTicketMessages(String userEmail, String ticketNumber) {
        return restClient.get()
                .uri("/api/v1/tickets/my/{ticketNumber}/messages", ticketNumber)
                .headers(headers -> addSecurityHeaders(headers, userEmail))
                .retrieve()
                .body(TicketMessageResponseDTO[].class);
    }
    
    private void addSecurityHeaders(org.springframework.http.HttpHeaders headers, String userEmail) {
        headers.add(HEADER_USER_EMAIL, userEmail);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            headers.add(HEADER_USER_ID, authentication.getName());
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");
            headers.add(HEADER_USER_ROLE, role);
        }
    }
}
