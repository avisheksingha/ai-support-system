package com.aisupport.analysis.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aisupport.analysis.model.AnalysisResult;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    
    Optional<AnalysisResult> findByTicketId(Long ticketId);
    
    List<AnalysisResult> findByIntent(String intent);
    
    List<AnalysisResult> findByUrgency(String urgency);
    
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.createdAt >= :startDate ORDER BY ar.createdAt DESC")
    List<AnalysisResult> findRecentAnalysis(LocalDateTime startDate);
    
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.urgency = :urgency")
    Long countByUrgency(String urgency);

}
