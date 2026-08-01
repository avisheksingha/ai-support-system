package com.aisupport.analysis.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.analysis.repository.AnalysisResultRepository;
import com.aisupport.common.dto.admin.AdminAnalysisStatsResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/analysis", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Admin Stats", description = "Internal endpoints for orchestration service")
public class AdminAnalysisStatsController {

    private final AnalysisResultRepository analysisResultRepository;

    @GetMapping("/stats/admin")
    public ResponseEntity<AdminAnalysisStatsResponse> getAdminStats() {
        log.info("Fetching admin analysis stats");
        
        long totalAnalyses = analysisResultRepository.count();
        long highConfidenceAnalyses = analysisResultRepository.countByConfidenceScoreGreaterThanEqual(new BigDecimal("0.70"));
        
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long processedToday = analysisResultRepository.countByCreatedAtAfter(startOfDay);

        AdminAnalysisStatsResponse response = AdminAnalysisStatsResponse.builder()
                .totalAnalyses(totalAnalyses)
                .highConfidenceAnalyses(highConfidenceAnalyses)
                .processedToday(processedToday)
                .build();

        return ResponseEntity.ok(response);
    }
}
