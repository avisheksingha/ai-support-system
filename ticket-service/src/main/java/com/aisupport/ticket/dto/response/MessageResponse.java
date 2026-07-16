package com.aisupport.ticket.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String ticketNumber;
    private String content;
    private String type; // e.g., CUSTOMER_MESSAGE, AGENT_MESSAGE, AI_DRAFT, INTERNAL_NOTE
    private boolean isInternal;
    private String senderId;
    private String senderName;
    private Instant createdAt;
}
