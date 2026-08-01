package com.aisupport.ticket.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    description = "Request payload for creating a support ticket", 
    requiredProperties = {"subject", "message"},
    example = "{\n  \"subject\": \"Cannot access my account\",\n  \"message\": \"I am unable to log in with my credentials.\"\n}"
)
public class TicketRequest {
    
    @Schema(description = "Ticket subject", example = "Cannot access my account")
    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;
    
    @Schema(description = "Detailed message for the support ticket", example = "I am unable to log in with my credentials.")
    @NotBlank(message = "Message is required")
    @Size(min = 10, message = "Message must be at least 10 characters")
    private String message;

    @Schema(description = "If true, bypasses soft validation warnings like TOO_SHORT", example = "false")
    private boolean bypassSoftValidation;
}
