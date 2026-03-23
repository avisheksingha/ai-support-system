package com.aisupport.ticket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aisupport.common.enums.TicketStatus;
import com.aisupport.ticket.dto.TicketRequest;
import com.aisupport.ticket.dto.TicketResponse;
import com.aisupport.ticket.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        TicketController controller = new TicketController(ticketService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createTicket_shouldReturnCreatedResponse() throws Exception {
        TicketResponse response = TicketResponse.builder()
                .id(1L)
                .ticketNumber("TKT-12345678")
                .customerEmail("user@example.com")
                .status(TicketStatus.NEW)
                .build();

        when(ticketService.createTicket(any(TicketRequest.class))).thenReturn(response);

        TicketRequest request = TicketRequest.builder()
                .customerEmail("user@example.com")
                .customerName("Jane Doe")
                .subject("Login issue")
                .message("I cannot login into my account")
                .build();

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketNumber").value("TKT-12345678"))
                .andExpect(jsonPath("$.customerEmail").value("user@example.com"));

        ArgumentCaptor<TicketRequest> captor = ArgumentCaptor.forClass(TicketRequest.class);
        verify(ticketService).createTicket(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Login issue");
    }

    @Test
    void getAllTickets_withStatus_shouldUseStatusFilter() throws Exception {
        TicketResponse response = TicketResponse.builder()
                .ticketNumber("TKT-1")
                .status(TicketStatus.NEW)
                .build();
        when(ticketService.getTicketsByStatus("NEW")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/tickets").param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketNumber").value("TKT-1"));

        verify(ticketService).getTicketsByStatus("NEW");
        verify(ticketService, never()).getAllTickets();
    }

    @Test
    void getAllTickets_withoutStatus_shouldReturnAllTickets() throws Exception {
        TicketResponse response = TicketResponse.builder()
                .ticketNumber("TKT-2")
                .status(TicketStatus.ASSIGNED)
                .build();
        when(ticketService.getAllTickets()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketNumber").value("TKT-2"));

        verify(ticketService).getAllTickets();
        verify(ticketService, never()).getTicketsByStatus(any());
    }
}
