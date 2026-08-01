package com.aisupport.orchestration.application.customer.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDashboardResponse {
    
    // We can reuse a simple profile structure, or define CustomerProfileDTO
    private CustomerProfileDTO profile;
    private CustomerSummaryDTO summary;
    private List<TicketSummaryDTO> tickets;
    private CustomerAssistanceDTO recommendedResources;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerProfileDTO {
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSummaryDTO {
        private int openRequests;
        private int waitingForSupport;
        private int resolved;
        private String latestTicketStatus;
        private String assignedSupportStatus;
        private String averageResponseTime; 
    }
}
