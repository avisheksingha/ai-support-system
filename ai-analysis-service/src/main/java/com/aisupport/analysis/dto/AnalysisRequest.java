package com.aisupport.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for AI analysis", requiredProperties = {"ticketId", "subject", "message"})
public class AnalysisRequest {
    
    @NotNull(message = "Ticket ID is required")
    @Schema(description = "Unique identifier for the ticket", example = "1")
    private Long ticketId;
    
    @NotBlank(message = "Subject is required")
    @Schema(description = "Subject of the support ticket", example = "Cannot access my account")
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Schema(description = "Detailed message for the support ticket", example = "I am unable to log in with my credentials.")
    private String message;

}
