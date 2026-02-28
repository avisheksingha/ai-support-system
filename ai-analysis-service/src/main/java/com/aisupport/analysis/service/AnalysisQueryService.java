package com.aisupport.analysis.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.analysis.mapper.AnalysisResultMapper;
import com.aisupport.analysis.repository.AnalysisResultRepository;
import com.aisupport.common.dto.AnalysisResultDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalysisQueryService {

    private final AnalysisResultRepository repository;
    private final AnalysisResultMapper mapper;

    @Transactional(readOnly = true)
    public AnalysisResultDTO getAnalysisByTicketId(Long ticketId) {
        return repository.findByTicketId(ticketId)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<AnalysisResultDTO> getAllAnalyses(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResultDTO> getAnalysesByIntent(String intent) {
        return repository.findByIntent(intent)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnalysisResultDTO> getAnalysesByUrgency(String urgency) {
        return repository.findByUrgency(urgency)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}