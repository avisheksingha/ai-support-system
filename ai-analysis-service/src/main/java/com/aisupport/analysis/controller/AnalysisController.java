package com.aisupport.analysis.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.analysis.service.AnalysisQueryService;
import com.aisupport.common.dto.AnalysisResultDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analysis", description = "AI ticket analysis query endpoints")
public class AnalysisController {
    
    private final AnalysisQueryService analysisService;
    
    @GetMapping("/ticket/{ticketId}")
    @Operation(
        summary = "Get analysis by ticket ID",
        description = "Retrieves the AI analysis result for a specific ticket ID"
    )
    public ResponseEntity<AnalysisResultDTO> getAnalysisByTicketId(
        @Parameter(description = "Numeric ID of the support ticket")
        @PathVariable Long ticketId) {
    	
    	log.info("Fetch analysis for ticketId: {}", ticketId);

        AnalysisResultDTO response =
                analysisService.getAnalysisByTicketId(ticketId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(
        summary = "Get all analyses",
        description = "Retrieves paginated analysis results with optional page and size parameters"
    )
    public ResponseEntity<Page<AnalysisResultDTO>> getAllAnalyses(
        @Parameter(description = "Page index (0-based)")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size (capped at 100)")
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(analysisService.getAllAnalyses(pageable));
    }

    @GetMapping("/intent/{intent}")
    @Operation(
        summary = "Get analyses by intent",
        description = "Retrieves analysis results filtered by predicted intent"
    )
    public ResponseEntity<List<AnalysisResultDTO>> getAnalysesByIntent(
        @Parameter(description = "Intent value: BILLING, TECHNICAL, GENERAL")
        @PathVariable String intent) {
    	
    	log.info("Fetch analyses by intent: {}", intent);

        return ResponseEntity.ok(
                analysisService.getAnalysesByIntent(intent.toUpperCase())
        );
    }

    @GetMapping("/urgency/{urgency}")
    @Operation(
        summary = "Get analyses by urgency",
        description = "Retrieves analysis results filtered by urgency level"
    )
    public ResponseEntity<List<AnalysisResultDTO>> getAnalysesByUrgency(
        @Parameter(description = "Urgency value: LOW, MEDIUM, HIGH, CRITICAL")
        @PathVariable String urgency) {
    	
    	log.info("Fetch analyses by urgency: {}", urgency);

        return ResponseEntity.ok(
                analysisService.getAnalysesByUrgency(urgency)
        );
    }
}
