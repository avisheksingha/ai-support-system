package com.aisupport.ticket.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aisupport.common.enums.TicketStatus;
import com.aisupport.ticket.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	Optional<Ticket> findByTicketNumber(String ticketNumber);
    
    List<Ticket> findByStatus(TicketStatus status);
    
    List<Ticket> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    
    Optional<Ticket> findByTicketNumberAndCustomerEmail(String ticketNumber, String customerEmail);
    
    List<Ticket> findByAssignedTo(String assignedTo);

    boolean existsByTicketNumber(String ticketNumber);

    @Query("SELECT t.assignedTo, COUNT(t) FROM Ticket t WHERE t.assignedTo IS NOT NULL GROUP BY t.assignedTo")
    List<Object[]> countTicketsByAssignedTo();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdAt >= :startOfDay")
    long countTicketsCreatedAfter(@Param("startOfDay") Instant startOfDay);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo IS NOT NULL AND t.assignedTo != 'Unassigned'")
    long countAssignedTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status NOT IN :statuses")
    long countByStatusNotIn(@Param("statuses") List<TicketStatus> statuses);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'RESOLVED' AND t.updatedAt >= :startOfDay")
    long countResolvedAfter(@Param("startOfDay") Instant startOfDay);
}
