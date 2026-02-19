package com.aisupport.analysis.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.analysis.dto.AnalysisRequest;
import com.aisupport.analysis.dto.ParsedAnalysis;
import com.aisupport.analysis.mapper.AnalysisResultMapper;
import com.aisupport.analysis.model.AnalysisResult;
import com.aisupport.analysis.repository.AnalysisResultRepository;
import com.aisupport.common.dto.AnalysisResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {
    
    private final GeminiService geminiService;
    private final AnalysisResultRepository analysisResultRepository;
    private final ObjectMapper objectMapper;
    private final AnalysisResultMapper analysisResultMapper;
    
    @Transactional
    public AnalysisResultDTO analyzeTicket(AnalysisRequest request) {
        log.info("Starting analysis for ticket ID: {}", request.getTicketId());
        
        // Check if already analyzed
        return analysisResultRepository.findByTicketId(request.getTicketId())
                .map(analysisResultMapper::toDto)
                .orElseGet(() -> performNewAnalysis(request));
    }
    
    private AnalysisResultDTO performNewAnalysis(AnalysisRequest request) {
        // Call Gemini AI
        ParsedAnalysis parsedAnalysis = geminiService.analyzeTicket(
                request.getSubject(), 
                request.getMessage()
        );
        
        // Save to database
        AnalysisResult analysisResult = AnalysisResult.builder()
                .ticketId(request.getTicketId())
                .intent(parsedAnalysis.getIntent())
                .sentiment(parsedAnalysis.getSentiment())
                .urgency(parsedAnalysis.getUrgency())
                .confidenceScore(BigDecimal.valueOf(parsedAnalysis.getConfidenceScore()))
                .keywords(parsedAnalysis.getKeywords().toArray(new String[0]))
                .suggestedCategory(parsedAnalysis.getSuggestedCategory())
                .rawResponse(convertToJson(parsedAnalysis))
                .build();
        
        analysisResult = analysisResultRepository.save(analysisResult);
        log.info("Analysis saved for ticket ID: {}", request.getTicketId());
        
        return analysisResultMapper.toDto(analysisResult);
    }
    
    @Transactional(readOnly = true)
    public AnalysisResultDTO getAnalysisByTicketId(Long ticketId) {
        log.info("Fetching analysis for ticket ID: {}", ticketId);
        return analysisResultRepository.findByTicketId(ticketId)
                .map(analysisResultMapper::toDto)
                .orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<AnalysisResultDTO> getAllAnalyses() {
        return analysisResultRepository.findAll().stream()
                .map(analysisResultMapper::toDto)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<AnalysisResultDTO> getAnalysesByIntent(String intent) {
        return analysisResultRepository.findByIntent(intent).stream()
                .map(analysisResultMapper::toDto)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<AnalysisResultDTO> getAnalysesByUrgency(String urgency) {
        return analysisResultRepository.findByUrgency(urgency).stream()
                .map(analysisResultMapper::toDto)
                .toList();
    }
    
    private String convertToJson(ParsedAnalysis analysis) {
        try {
            return objectMapper.writeValueAsString(analysis);
        } catch (Exception e) {
            log.error("Failed to convert analysis to JSON", e);
            return "{}";
        }
    }
}