package com.aisupport.ticket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aisupport.ticket.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
	Optional<Ticket> findByTicketNumber(String ticketNumber);
    
    List<Ticket> findByStatus(Ticket.TicketStatus status);
    
    List<Ticket> findByCustomerEmail(String customerEmail);
    
    List<Ticket> findByAssignedTo(String assignedTo);
}
