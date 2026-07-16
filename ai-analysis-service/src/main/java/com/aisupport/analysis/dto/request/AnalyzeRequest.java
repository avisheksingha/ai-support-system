package com.aisupport.analysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalyzeRequest {
    
    @NotNull(message = "ticketId must not be null")
    private Long ticketId;
    
    @NotBlank(message = "subject must not be blank")
    private String subject;
    
    @NotBlank(message = "message must not be blank")
    private String message;
}
