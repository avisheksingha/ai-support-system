package com.aisupport.orchestration.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;

public interface AiExecutionRecordRepository extends JpaRepository<AiExecutionRecordEntity, String> {
    List<AiExecutionRecordEntity> findByCorrelationId(String correlationId);
    java.util.Optional<AiExecutionRecordEntity> findTopByTicketIdOrderByExecutedAtDesc(Long ticketId);
}
