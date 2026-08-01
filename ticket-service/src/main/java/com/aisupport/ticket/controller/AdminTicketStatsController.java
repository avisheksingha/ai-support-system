package com.aisupport.ticket.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.common.dto.admin.AdminTicketStatsResponse;
import com.aisupport.common.enums.TicketStatus;
import com.aisupport.ticket.repository.TicketRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Admin Stats", description = "Internal endpoints for orchestration service")
public class AdminTicketStatsController {

    private final TicketRepository ticketRepository;

    @GetMapping("/stats/admin")
    public ResponseEntity<AdminTicketStatsResponse> getAdminStats() {
        log.info("Fetching admin ticket stats");
        
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long ticketsToday = ticketRepository.countTicketsCreatedAfter(startOfDay);

        List<Object[]> assignments = ticketRepository.countTicketsByAssignedTo();
        Map<String, Long> departmentWorkload = new HashMap<>();
        Map<String, Long> routingOverview = new HashMap<>();

        for (Object[] result : assignments) {
            String assignedTo = (String) result[0];
            Long count = (Long) result[1];
            
            departmentWorkload.put(assignedTo, count);
            routingOverview.put(assignedTo + " Routed", count);
        }
        
        // Mocking manual overrides for now as it's not directly tracked in tickets table.
        routingOverview.put("Manual Overrides", 0L);

        long totalTickets = ticketRepository.count();
        long assignedTickets = ticketRepository.countAssignedTickets();
        long activeTickets = ticketRepository.countByStatusNotIn(List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
        long resolvedToday = ticketRepository.countResolvedAfter(startOfDay);

        AdminTicketStatsResponse response = AdminTicketStatsResponse.builder()
                .ticketsToday(ticketsToday)
                .totalTickets(totalTickets)
                .assignedTickets(assignedTickets)
                .activeTickets(activeTickets)
                .resolvedToday(resolvedToday)
                .departmentWorkload(departmentWorkload)
                .routingOverview(routingOverview)
                .build();

        return ResponseEntity.ok(response);
    }
}
