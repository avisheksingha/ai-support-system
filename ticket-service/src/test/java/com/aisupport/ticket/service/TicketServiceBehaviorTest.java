package com.aisupport.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.enums.TicketStatus;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.ticket.entity.Ticket;
import com.aisupport.ticket.exception.InvalidTicketInputException;
import com.aisupport.ticket.mapper.TicketMapper;
import com.aisupport.ticket.outbox.OutboxEventService;
import com.aisupport.ticket.repository.TicketRepository;

class TicketServiceBehaviorTest {

    private TicketRepository ticketRepository;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        TicketMapper ticketMapper = mock(TicketMapper.class);
        OutboxEventService outboxEventService = mock(OutboxEventService.class);
        ticketService = new TicketService(ticketRepository, ticketMapper, outboxEventService);
    }

    @Test
    void getTicketsByStatus_shouldRejectInvalidStatus() {
        assertThatThrownBy(() -> ticketService.getTicketsByStatus("NOT_A_STATUS"))
                .isInstanceOf(InvalidTicketInputException.class)
                .hasMessageContaining("Invalid status");
    }

    @Test
    void updateTicketPriority_shouldRejectInvalidPriority() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setTicketNumber("TKT-TEST");

        when(ticketRepository.findByTicketNumber("TKT-TEST"))
                .thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.updateTicketPriority("TKT-TEST", "NOPE", null))
                .isInstanceOf(InvalidTicketInputException.class)
                .hasMessageContaining("Invalid priority");
    }

    @Test
    void applyRoutingResult_shouldNoOpWhenTicketAlreadyBeyondAssigned() {
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTicketNumber("TKT-10");
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAssignedTo("agent-a");
        ticket.setPriority(TicketPriority.HIGH);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        TicketRoutedEvent event = TicketRoutedEvent.builder()
                .ticketId(10L)
                .assignToTeam("billing-team")
                .priority(TicketPriority.CRITICAL)
                .slaHours(4)
                .intent("PAYMENT_ISSUE")
                .sentiment("NEGATIVE")
                .urgency("HIGH")
                .build();

        ticketService.applyRoutingResult(event);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(ticket.getAssignedTo()).isEqualTo("agent-a");
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
    }
}
