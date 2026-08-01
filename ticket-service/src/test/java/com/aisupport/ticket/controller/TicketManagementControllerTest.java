package com.aisupport.ticket.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aisupport.common.enums.TicketStatus;
import com.aisupport.ticket.dto.response.TicketResponse;
import com.aisupport.ticket.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TicketManagementControllerTest {

    @Mock
    private TicketService ticketService;

    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
    	TicketManagementController controller = new TicketManagementController(ticketService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        new ObjectMapper();
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
