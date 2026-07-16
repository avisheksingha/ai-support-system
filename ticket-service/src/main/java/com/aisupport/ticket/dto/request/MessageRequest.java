package com.aisupport.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    
    @NotBlank(message = "Message content cannot be empty")
    private String content;
    
    private boolean isInternal;
    
    private String senderId;
    
    private String senderName;
}
