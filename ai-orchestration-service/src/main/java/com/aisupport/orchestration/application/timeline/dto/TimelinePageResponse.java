package com.aisupport.orchestration.application.timeline.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response for workflow timeline events")
public class TimelinePageResponse {
    @Schema(description = "List of timeline events")
    private List<TimelineEvent> content;
    
    @Schema(description = "Current page number", example = "0")
    private int pageNumber;
    
    @Schema(description = "Page size", example = "50")
    private int pageSize;
    
    @Schema(description = "Total elements across all pages", example = "5")
    private long totalElements;
    
    @Schema(description = "Total number of pages", example = "1")
    private int totalPages;
    
    @Schema(description = "Indicates if this is the last page", example = "true")
    private boolean isLast;
}
