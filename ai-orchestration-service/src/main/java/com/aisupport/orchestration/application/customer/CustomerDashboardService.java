package com.aisupport.orchestration.application.customer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aisupport.orchestration.application.customer.dto.CustomerAssistanceDTO;
import com.aisupport.orchestration.application.customer.dto.CustomerDashboardResponse;
import com.aisupport.orchestration.application.customer.dto.CustomerDashboardResponse.CustomerProfileDTO;
import com.aisupport.orchestration.application.customer.dto.CustomerDashboardResponse.CustomerSummaryDTO;
import com.aisupport.orchestration.application.customer.dto.CustomerTicketDetailResponse;
import com.aisupport.orchestration.application.customer.dto.CustomerTicketDetailResponse.MessageDTO;
import com.aisupport.orchestration.application.customer.dto.TicketDetailDTO;
import com.aisupport.orchestration.application.customer.dto.TicketSummaryDTO;
import com.aisupport.orchestration.infrastructure.client.TicketClient;
import com.aisupport.orchestration.infrastructure.client.dto.TicketMessageResponseDTO;
import com.aisupport.orchestration.infrastructure.client.dto.TicketResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerDashboardService {

    private final TicketClient ticketClient;

    public CustomerDashboardResponse getCustomerDashboard(String userEmail, String userName) {
        TicketResponseDTO[] ticketsArray = null;
        try {
            ticketsArray = ticketClient.getCustomerTickets(userEmail);
        } catch (Exception e) {
            log.error("Failed to fetch customer tickets", e);
        }
        
        List<TicketResponseDTO> allTickets = ticketsArray != null ? Arrays.asList(ticketsArray) : Collections.emptyList();
        
        // Sort newest first
        allTickets.sort(Comparator.comparing(TicketResponseDTO::getCreatedAt).reversed());

        int openCount = 0;
        int waitingCount = 0;
        int resolvedCount = 0;
        String latestStatus = "None";
        String assignedSupportStatus = "Pending";

        for (TicketResponseDTO t : allTickets) {
            String s = t.getStatus();
            if (Arrays.asList("NEW", "ANALYZING", "ANALYZED", "ASSIGNED", "IN_PROGRESS").contains(s)) {
                openCount++;
            }
            if (Arrays.asList("NEW", "ANALYZING", "ANALYZED").contains(s)) {
                waitingCount++;
            }
            if (Arrays.asList("RESOLVED", "CLOSED").contains(s)) {
                resolvedCount++;
            }
        }

        List<TicketSummaryDTO> ticketSummaries = allTickets.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
                
        if (!ticketSummaries.isEmpty()) {
            latestStatus = formatStatus(ticketSummaries.get(0).getStatus());
            assignedSupportStatus = ticketSummaries.get(0).getAssignedSupportStatus();
        }

        CustomerAssistanceDTO recommendedResources = null;
        for (TicketResponseDTO t : allTickets) {
            if (t.getRagResponse() != null && !t.getRagResponse().isBlank()) {
                recommendedResources = extractAssistance(t);
                break;
            }
        }

        CustomerSummaryDTO summary = CustomerSummaryDTO.builder()
                .openRequests(openCount)
                .waitingForSupport(waitingCount)
                .resolved(resolvedCount)
                .latestTicketStatus(latestStatus)
                .assignedSupportStatus(assignedSupportStatus)
                .averageResponseTime("4 hours") // Example business metric
                .build();

        return CustomerDashboardResponse.builder()
                .profile(new CustomerProfileDTO(userName, userEmail))
                .summary(summary)
                .tickets(ticketSummaries)
                .recommendedResources(recommendedResources)
                .build();
    }

    public CustomerTicketDetailResponse getCustomerTicketDetail(String userEmail, String ticketNumber) {
        TicketResponseDTO ticket = ticketClient.getCustomerTicket(userEmail, ticketNumber);
        TicketMessageResponseDTO[] messagesArray = ticketClient.getTicketMessages(userEmail, ticketNumber);
        List<TicketMessageResponseDTO> messages = messagesArray != null ? Arrays.asList(messagesArray) : Collections.emptyList();

        List<MessageDTO> messageDTOs = messages.stream()
                .filter(m -> !Boolean.TRUE.equals(m.getIsInternal()) && !"INTERNAL_NOTE".equals(m.getType()))
                .map(m -> MessageDTO.builder()
                        .id(m.getId())
                        .content(m.getContent())
                        .senderName(m.getSenderName())
                        .type(m.getType())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        TicketDetailDTO detailDTO = TicketDetailDTO.builder()
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .status(ticket.getStatus())
                .priority(formatPriority(ticket.getPriority()))
                .assignedSupportStatus(formatAssignedTo(ticket.getAssignedTo()))
                .estimatedResponse(formatSla(ticket.getSlaHours()))
                .createdAt(ticket.getCreatedAt())
                .lastUpdated(ticket.getUpdatedAt() != null ? ticket.getUpdatedAt() : ticket.getCreatedAt())
                .build();

        CustomerAssistanceDTO assistance = null;
        if (ticket.getRagResponse() != null && !ticket.getRagResponse().isBlank()) {
            assistance = extractAssistance(ticket);
        }

        return CustomerTicketDetailResponse.builder()
                .ticket(detailDTO)
                .messages(messageDTOs)
                .customerAssistance(assistance)
                .build();
    }

    private TicketSummaryDTO mapToSummary(TicketResponseDTO ticket) {
        return TicketSummaryDTO.builder()
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .status(ticket.getStatus())
                .priority(formatPriority(ticket.getPriority()))
                .assignedSupportStatus(formatAssignedTo(ticket.getAssignedTo()))
                .estimatedResponse(formatSla(ticket.getSlaHours()))
                .lastUpdated(ticket.getUpdatedAt() != null ? ticket.getUpdatedAt() : ticket.getCreatedAt())
                .build();
    }

    private String formatPriority(String priority) {
        if (priority == null) return "Standard";
        // capitalize first letter
        if (priority.length() > 1) {
            return priority.substring(0, 1).toUpperCase() + priority.substring(1).toLowerCase();
        }
        return priority;
    }

    private String formatAssignedTo(String assignedTo) {
        if (assignedTo == null || assignedTo.isBlank()) {
            return "Pending Assignment";
        }
        return "Support Specialist"; // Mask internal team names
    }

    private String formatSla(Integer slaHours) {
        if (slaHours == null) return "Standard";
        return "Within " + slaHours + " hours";
    }

    private String formatStatus(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case "NEW":
            case "ANALYZING":
            case "ANALYZED":
                return "Submitted";
            case "ASSIGNED":
                return "In Review";
            case "IN_PROGRESS":
                return "In Progress";
            case "RESOLVED":
                return "Resolved";
            case "CLOSED":
                return "Closed";
            default:
                return status;
        }
    }

    private CustomerAssistanceDTO extractAssistance(TicketResponseDTO ticket) {
        return CustomerAssistanceDTO.builder()
                .title("Helpful Information")
                .summary(ticket.getRagResponse())
                .build();
    }
}
