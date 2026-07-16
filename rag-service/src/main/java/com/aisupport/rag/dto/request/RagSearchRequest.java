package com.aisupport.rag.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RagSearchRequest {
    
    @NotNull(message = "ticketId must not be null")
    private Long ticketId;
    
    @NotBlank(message = "query must not be blank")
    private String query;
}
