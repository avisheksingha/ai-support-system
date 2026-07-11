package com.aisupport.orchestration.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;

import java.util.List;

@Repository
public interface AiExecutionRecordRepository extends JpaRepository<AiExecutionRecordEntity, String> {
    List<AiExecutionRecordEntity> findByCorrelationId(String correlationId);
}
