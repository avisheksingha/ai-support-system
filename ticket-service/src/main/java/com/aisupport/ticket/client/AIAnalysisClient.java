package com.aisupport.ticket.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.aisupport.common.dto.AnalysisResultDTO;

@FeignClient(name = "AI-ANALYSIS-SERVICE", url = "${api.ai.analysis.service.url}")
public interface AIAnalysisClient {
	
	@PostMapping("/api/v1/analysis/analyze")
    AnalysisResultDTO analyzeTicket(@RequestBody AnalysisRequest request);
}
