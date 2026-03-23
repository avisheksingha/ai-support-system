package com.aisupport.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.aisupport.analysis.entity.AnalysisResult;
import com.aisupport.analysis.mapper.AnalysisResultMapper;
import com.aisupport.analysis.repository.AnalysisResultRepository;
import com.aisupport.common.dto.AnalysisResultDTO;

@ExtendWith(MockitoExtension.class)
class AnalysisQueryServiceTest {

    @Mock
    private AnalysisResultRepository repository;
    @Mock
    private AnalysisResultMapper mapper;

    private AnalysisQueryService service;

    @BeforeEach
    void setUp() {
        service = new AnalysisQueryService(repository, mapper);
    }

    @Test
    void getAnalysisByTicketId_shouldMapEntityToDto() {
        AnalysisResult entity = AnalysisResult.builder().ticketId(11L).intent("PAYMENT_ISSUE").build();
        AnalysisResultDTO dto = AnalysisResultDTO.builder().ticketId(11L).intent("PAYMENT_ISSUE").build();

        when(repository.findByTicketId(11L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        AnalysisResultDTO result = service.getAnalysisByTicketId(11L);

        assertThat(result).isNotNull();
        assertThat(result.getIntent()).isEqualTo("PAYMENT_ISSUE");
    }

    @Test
    void getAnalysesByUrgency_shouldQueryRepositoryAndMapResults() {
        AnalysisResult entity = AnalysisResult.builder().ticketId(99L).urgency("HIGH").build();
        AnalysisResultDTO dto = AnalysisResultDTO.builder().ticketId(99L).urgency("HIGH").build();

        when(repository.findByUrgency("HIGH")).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<AnalysisResultDTO> result = service.getAnalysesByUrgency("HIGH");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicketId()).isEqualTo(99L);
        verify(repository).findByUrgency("HIGH");
    }

    @Test
    void getAllAnalyses_shouldReturnMappedPage() {
        AnalysisResult entity = AnalysisResult.builder().ticketId(5L).build();
        AnalysisResultDTO dto = AnalysisResultDTO.builder().ticketId(5L).build();

        when(repository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        var page = service.getAllAnalyses(PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTicketId()).isEqualTo(5L);
    }
}
