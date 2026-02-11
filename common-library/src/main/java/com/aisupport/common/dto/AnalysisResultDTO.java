package com.aisupport.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
	
	private Long id;
    private Long ticketId;
    private String intent;
    private String sentiment;
    private String urgency;
    private BigDecimal confidenceScore;
    private List<String> keywords; // converted from String[]
    private String suggestedCategory;
    private String rawResponse;
    
    private String analysisProvider; // always "Gemini AI"
    private LocalDateTime analyzedAt; // maps from createdAt

}
