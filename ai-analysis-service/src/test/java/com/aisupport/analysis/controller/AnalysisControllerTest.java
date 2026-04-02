package com.aisupport.analysis.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aisupport.analysis.service.AnalysisQueryService;
import com.aisupport.common.dto.AnalysisResultDTO;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerTest {

    @Mock
    private AnalysisQueryService analysisQueryService;

    private AnalysisController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new AnalysisController(analysisQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAnalysisByTicketId_whenMissing_shouldReturnNotFound() throws Exception {
        when(analysisQueryService.getAnalysisByTicketId(100L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/analysis/ticket/100"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAnalyses_shouldCapRequestedSizeTo100() {
        AnalysisResultDTO dto = AnalysisResultDTO.builder().ticketId(1L).intent("PAYMENT_ISSUE").build();
        when(analysisQueryService.getAllAnalyses(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        ResponseEntity<org.springframework.data.domain.Page<AnalysisResultDTO>> response =
                controller.getAllAnalyses(0, 1000);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(analysisQueryService).getAllAnalyses(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void getAnalysesByIntent_shouldNormalizeIntentToUppercase() throws Exception {
        when(analysisQueryService.getAnalysesByIntent("PAYMENT_ISSUE"))
                .thenReturn(List.of(AnalysisResultDTO.builder().ticketId(5L).build()));

        mockMvc.perform(get("/api/v1/analysis/intent/payment_issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketId").value(5));

        verify(analysisQueryService).getAnalysesByIntent("PAYMENT_ISSUE");
    }
}
