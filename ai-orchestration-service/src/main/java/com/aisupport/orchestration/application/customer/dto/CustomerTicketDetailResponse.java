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
public class CustomerTicketDetailResponse {
    private TicketDetailDTO ticket;
    private List<MessageDTO> messages; // We need to define MessageDTO inside or reuse a common one. We'll define it inside.
    private CustomerAssistanceDTO customerAssistance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDTO {
        private Long id;
        private String content;
        private String senderName;
        private String type; // "CUSTOMER_MESSAGE", "AGENT_MESSAGE", "SYSTEM_MESSAGE"
        private String createdAt;
    }
}
