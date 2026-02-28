package com.aisupport.analysis.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.analysis.service.AnalysisQueryService;
import com.aisupport.common.dto.AnalysisResultDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/analysis")
@CrossOrigin(origins = "http://localhost:8083")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analysis", description = "AI ticket analysis query endpoints")
public class AnalysisController {
    
    private final AnalysisQueryService analysisService;
    
    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "Get analysis result by ticket ID")
    public ResponseEntity<AnalysisResultDTO> getAnalysisByTicketId(
            @PathVariable Long ticketId) {

        AnalysisResultDTO response =
                analysisService.getAnalysisByTicketId(ticketId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all analyses")
    public ResponseEntity<Page<AnalysisResultDTO>> getAllAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
    	int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);
        return ResponseEntity.ok(analysisService.getAllAnalyses(pageable));
    }

    @GetMapping("/intent/{intent}")
    @Operation(summary = "Get analyses by intent")
    public ResponseEntity<List<AnalysisResultDTO>> getAnalysesByIntent(
            @PathVariable String intent) {

        return ResponseEntity.ok(
                analysisService.getAnalysesByIntent(intent.toUpperCase())
        );
    }

    @GetMapping("/urgency/{urgency}")
    @Operation(summary = "Get analyses by urgency")
    public ResponseEntity<List<AnalysisResultDTO>> getAnalysesByUrgency(
            @PathVariable String urgency) {

        return ResponseEntity.ok(
                analysisService.getAnalysesByUrgency(urgency)
        );
    }
}