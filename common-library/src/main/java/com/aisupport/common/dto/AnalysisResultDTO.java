package com.aisupport.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResultDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Schema(description = "ID of the analysis result", example = "1")
	private Long id;
	
	@Schema(description = "ID of the ticket", example = "1")
    private Long ticketId;
	
	@Schema(description = "Intent of the analysis result", example = "Order Status")
    private String intent;
	
	@Schema(description = "Sentiment of the analysis result", example = "Positive")
    private String sentiment;
	
	@Schema(description = "Urgency of the analysis result", example = "High")
    private String urgency;
	
	@Schema(description = "Confidence score of the analysis result", example = "0.9")
    private BigDecimal confidenceScore;
	
	@Schema(description = "Keywords of the analysis result", example = "[\"Order Status\"]")
    private List<String> keywords; // converted from String[]
	
	@Schema(description = "Suggested category of the analysis result", example = "Order Status")
    private String suggestedCategory;
	
	@Schema(description = "Raw response of the analysis result", example = "Order Status")
    private String rawResponse;
    
	@Schema(description = "Analysis provider of the analysis result", example = "Google GenAI")
    private String analysisProvider; // always "Google GenAI"
	
	@Schema(description = "Analyzed at of the analysis result", example = "2022-01-01T00:00:00")
    private Instant analyzedAt; // maps from createdAt

}
