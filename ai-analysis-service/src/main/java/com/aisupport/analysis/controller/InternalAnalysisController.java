package com.aisupport.analysis.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.analysis.dto.request.AnalyzeRequest;
import com.aisupport.analysis.service.AnalysisProcessingService;
import com.aisupport.analysis.service.AnalysisQueryService;
import com.aisupport.common.dto.AnalysisResultDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/analysis", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Analysis", description = "Internal endpoints for orchestration service")
public class InternalAnalysisController {

    private final AnalysisProcessingService analysisService;
    private final AnalysisQueryService analysisQueryService;

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<AnalysisResultDTO> getAnalysisByTicketId(@PathVariable Long ticketId) {
        log.info("Internal REST request to fetch analysis for ticketId: {}", ticketId);
        AnalysisResultDTO response = analysisQueryService.getAnalysisByTicketId(ticketId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResultDTO> analyzeTicket(@Valid @RequestBody AnalyzeRequest request) {
        
        int msgLength = request.getMessage() != null ? request.getMessage().length() : 0;
        log.info("Internal REST request to analyze ticketId={}, msgLength={}", request.getTicketId(), msgLength);
        
        AnalysisResultDTO response = analysisService.analyzeTicketSync(
                request.getTicketId(), 
                request.getSubject(), 
                request.getMessage());
                
        return ResponseEntity.ok(response);
    }
}
