package com.aisupport.orchestration.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessageResponseDTO {
    private Long id;
    private Long ticketId;
    private String content;
    private String senderId;
    private String senderName;
    private String type; // CUSTOMER_MESSAGE, AGENT_MESSAGE, SYSTEM_MESSAGE
    private Boolean isInternal;
    private String createdAt;
}
