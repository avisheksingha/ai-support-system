package com.aisupport.analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aisupport.analysis.dto.request.WritingContext;
import com.aisupport.analysis.dto.request.WritingImproveRequest;
import com.aisupport.analysis.dto.response.WritingImproveResponse;
import com.aisupport.analysis.service.WritingAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class WritingAssistantControllerTest {

    @Mock
    private WritingAssistantService writingAssistantService;

    private WritingAssistantController controller;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new WritingAssistantController(writingAssistantService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void improveWriting_shouldReturnImprovedText() throws Exception {
        WritingImproveRequest request = new WritingImproveRequest(
                WritingContext.SUPPORT_TICKET,
                "Help",
                "I needs help",
                "en"
        );

        WritingImproveResponse response = new WritingImproveResponse(
                "Help",
                "I need help.",
                List.of("Corrected grammar"),
                true,
                "Gemini 2.5"
        );

        when(writingAssistantService.improve(any(WritingImproveRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/analysis/writing/improve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.improvedContent").value("I need help."));
    }
}
