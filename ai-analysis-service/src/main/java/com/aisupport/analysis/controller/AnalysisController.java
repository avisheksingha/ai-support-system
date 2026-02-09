package com.aisupport.analysis.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.analysis.dto.AnalysisRequest;
import com.aisupport.analysis.service.AnalysisService;
import com.aisupport.common.dto.AnalysisResultDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/analysis")
@CrossOrigin(origins = "http://localhost:8083")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analysis", description = "AI-powered ticket analysis endpoints")
public class AnalysisController {
    
    private final AnalysisService analysisService;
    
    @PostMapping("/analyze")
    @Operation(summary = "Analyze a ticket using AI")
    public ResponseEntity<AnalysisResultDTO> analyzeTicket(
            @Valid @RequestBody AnalysisRequest request) {
        log.info("Received analysis request for ticket ID: {}", request.getTicketId());
        AnalysisResultDTO response = analysisService.analyzeTicket(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "Get analysis result by ticket ID")
    public ResponseEntity<AnalysisResultDTO> getAnalysisByTicketId(
            @PathVariable Long ticketId) {
        log.info("Fetching analysis for ticket ID: {}", ticketId);
        AnalysisResultDTO response = analysisService.getAnalysisByTicketId(ticketId);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all analyses")
    public ResponseEntity<List<AnalysisResultDTO>> getAllAnalyses() {
        log.info("Fetching all analyses");
        List<AnalysisResultDTO> responses = analysisService.getAllAnalyses();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/intent/{intent}")
    @Operation(summary = "Get analyses by intent")
    public ResponseEntity<List<AnalysisResultDTO>> getAnalysesByIntent(
            @PathVariable String intent) {
        log.info("Fetching analyses with intent: {}", intent);
        List<AnalysisResultDTO> responses = analysisService.getAnalysesByIntent(intent);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/urgency/{urgency}")
    @Operation(summary = "Get analyses by urgency")
    public ResponseEntity<List<AnalysisResultDTO>> getAnalysesByUrgency(
            @PathVariable String urgency) {
        log.info("Fetching analyses with urgency: {}", urgency);
        List<AnalysisResultDTO> responses = analysisService.getAnalysesByUrgency(urgency);
        return ResponseEntity.ok(responses);
    }
}