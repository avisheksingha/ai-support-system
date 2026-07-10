package com.aisupport.analysis.writing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for writing improvement")
public record WritingImproveRequest(
        @Schema(description = "The context in which the text is being written", example = "SUPPORT_TICKET", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Context is required")
        WritingContext context,

        @Schema(description = "The subject or title of the text", example = "Login issue", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Subject is required")
        @Size(max = 500, message = "Subject cannot exceed 500 characters")
        String subject,

        @Schema(description = "The main body content to be improved", example = "I cant login today", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Content cannot exceed 5000 characters")
        String content,
        
        @Schema(description = "Language or locale to assist with context (e.g. en-US)", example = "en-US")
        String language
) {
}
