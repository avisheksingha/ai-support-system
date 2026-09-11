package com.aisupport.routing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.aisupport.routing.entity.RoutingRule;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long>, JpaSpecificationExecutor<RoutingRule> {
    
    Optional<RoutingRule> findByRuleName(String ruleName);
    
    List<RoutingRule> findByActiveTrue();
    
    @Query("SELECT r FROM RoutingRule r WHERE r.active = true ORDER BY r.priority DESC")
    List<RoutingRule> findActiveRulesOrderedByPriority();
    
    List<RoutingRule> findByIntentPattern(String intentPattern);
    
    List<RoutingRule> findByUrgencyPattern(String urgencyPattern);
    
    @Query("SELECT COUNT(r) FROM RoutingRule r WHERE r.active = true")
    Long countActiveRules();
    
    Long countByActive(Boolean active);

    boolean existsByRuleName(String ruleName);

    @Query("SELECT DISTINCT r.assignToTeam FROM RoutingRule r WHERE r.assignToTeam IS NOT NULL ORDER BY r.assignToTeam")
    List<String> findDistinctAssignToTeams();
}
