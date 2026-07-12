package com.aisupport.rag.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aisupport.rag.entity.RagResponse;

public interface RagResponseRepository extends JpaRepository<RagResponse, Long> {

    Optional<RagResponse> findTopByTicketIdOrderByCreatedAtDesc(Long ticketId);
}