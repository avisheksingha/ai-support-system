package com.aisupport.ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aisupport.ticket.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
