package com.aisupport.rule.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aisupport.rule.entity.RuleExecutionHistory;

@Repository
public interface RuleExecutionHistoryRepository extends JpaRepository<RuleExecutionHistory, Long> {
    
    List<RuleExecutionHistory> findByRuleId(Long ruleId);
    
    List<RuleExecutionHistory> findByTicketId(Long ticketId);
    
    @Query("SELECT h FROM RuleExecutionHistory h WHERE h.executedAt >= :startDate ORDER BY h.executedAt DESC")
    List<RuleExecutionHistory> findRecentExecutions(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(h) FROM RuleExecutionHistory h WHERE h.ruleId = :ruleId AND h.matched = true")
    Long countMatchedExecutions(@Param("ruleId") Long ruleId);
    
    @Query("SELECT AVG(h.executionTimeNs) FROM RuleExecutionHistory h WHERE h.ruleId = :ruleId")
    Double averageExecutionTime(@Param("ruleId") Long ruleId);
}
