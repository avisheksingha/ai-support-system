package com.aisupport.analysis.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.analysis.service.AnalysisProcessingService;
import com.aisupport.common.dto.AnalysisResultDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/analysis", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class InternalAnalysisController {

    private final AnalysisProcessingService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResultDTO> analyzeTicket(@RequestBody AnalyzeRequest request) {
        log.info("Internal REST request to analyze ticketId: {}", request.getTicketId());
        
        AnalysisResultDTO response = analysisService.analyzeTicketSync(
                request.getTicketId(), 
                request.getSubject(), 
                request.getMessage());
                
        return ResponseEntity.ok(response);
    }

    @Data
    public static class AnalyzeRequest {
        private Long ticketId;
        private String subject;
        private String message;
    }
}
