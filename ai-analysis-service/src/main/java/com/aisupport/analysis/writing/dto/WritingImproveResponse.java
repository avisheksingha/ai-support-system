package com.aisupport.analysis.writing.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the improved text and metadata")
public record WritingImproveResponse(
        @Schema(description = "The improved subject", example = "Login issue")
        String improvedSubject,
        
        @Schema(description = "The improved content", example = "I cannot login today.")
        String improvedContent,
        
        @Schema(description = "List of changes made to the original text")
        List<String> changes,
        
        @Schema(description = "Indicates whether the text was actually modified/improved", example = "true")
        boolean improved,
        
        @Schema(description = "The AI model used for the improvement", example = "gpt-4o")
        String model
) {
}
