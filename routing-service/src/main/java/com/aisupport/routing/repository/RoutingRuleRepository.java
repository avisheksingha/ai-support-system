package com.aisupport.routing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aisupport.routing.entity.RoutingRule;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {
    
    Optional<RoutingRule> findByRuleName(String ruleName);
    
    List<RoutingRule> findByActiveTrue();
    
    @Query("SELECT r FROM RoutingRule r WHERE r.active = true ORDER BY r.priority DESC")
    List<RoutingRule> findActiveRulesOrderedByPriority();
    
    List<RoutingRule> findByIntentPattern(String intentPattern);
    
    List<RoutingRule> findByUrgencyPattern(String urgencyPattern);
    
    @Query("SELECT COUNT(r) FROM RoutingRule r WHERE r.active = true")
    Long countActiveRules();
    
    boolean existsByRuleName(String ruleName);
}
