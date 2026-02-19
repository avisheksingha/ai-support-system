package com.aisupport.rule.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.rule.dto.RuleEvaluationRequest;
import com.aisupport.rule.dto.RuleEvaluationResponse;
import com.aisupport.rule.model.RoutingRule;
import com.aisupport.rule.model.RuleExecutionHistory;
import com.aisupport.rule.repository.RoutingRuleRepository;
import com.aisupport.rule.repository.RuleExecutionHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluationService {
    
    private final RoutingRuleRepository routingRuleRepository;
    private final RuleExecutionHistoryRepository historyRepository;
    
    @Transactional
    public RuleEvaluationResponse evaluateRules(RuleEvaluationRequest request) {
        log.info("Evaluating rules for ticket ID: {}", request.getTicketId());
        
        long startTime = System.currentTimeMillis();
        
        List<RoutingRule> activeRules = routingRuleRepository.findActiveRulesOrderedByPriority();
        log.debug("Found {} active rules to evaluate", activeRules.size());
        
        for (RoutingRule rule : activeRules) {
            long ruleStartTime = System.currentTimeMillis();
            boolean matches = evaluateRule(rule, request);
            long ruleExecutionTime = System.currentTimeMillis() - ruleStartTime;
            
            // Save execution history
            saveExecutionHistory(rule.getId(), request.getTicketId(), matches, ruleExecutionTime);
            
            if (matches) {
                long totalTime = System.currentTimeMillis() - startTime;
                log.info("Rule '{}' matched for ticket {}", rule.getRuleName(), request.getTicketId());
                
                return RuleEvaluationResponse.builder()
                        .ticketId(request.getTicketId())
                        .matchedRuleId(rule.getId())
                        .matchedRuleName(rule.getRuleName())
                        .assignToTeam(rule.getAssignToTeam())
                        .priorityOverride(rule.getPriorityOverride())
                        .slaHours(rule.getSlaHours())
                        .reason(buildMatchReason(rule, request))
                        .evaluationTimeMs(totalTime)
                        .build();
            }
        }
        
        // No rule matched - return default
        long totalTime = System.currentTimeMillis() - startTime;
        log.warn("No rule matched for ticket {}, using default assignment", request.getTicketId());
        
        return RuleEvaluationResponse.builder()
                .ticketId(request.getTicketId())
                .matchedRuleId(null)
                .matchedRuleName("Default Assignment")
                .assignToTeam("general-support")
                .priorityOverride("MEDIUM")
                .slaHours(24)
                .reason("No matching rule found, using default assignment")
                .evaluationTimeMs(totalTime)
                .build();
    }
    
    private boolean evaluateRule(RoutingRule rule, RuleEvaluationRequest request) {
        // Check intent pattern
        if (rule.getIntentPattern() != null && !rule.getIntentPattern().isEmpty()) {
            if (!matchesPattern(rule.getIntentPattern(), request.getIntent())) {
                return false;
            }
        }
        
        // Check sentiment pattern
        if (rule.getSentimentPattern() != null && !rule.getSentimentPattern().isEmpty()) {
            if (!matchesPattern(rule.getSentimentPattern(), request.getSentiment())) {
                return false;
            }
        }
        
        // Check urgency pattern
        if (rule.getUrgencyPattern() != null && !rule.getUrgencyPattern().isEmpty()) {
            if (!matchesPattern(rule.getUrgencyPattern(), request.getUrgency())) {
                return false;
            }
        }
        
        // Check keyword patterns
        if (rule.getKeywordPatterns() != null && rule.getKeywordPatterns().length > 0) {
            if (!matchesKeywords(rule.getKeywordPatterns(), request.getKeywords())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean matchesPattern(String pattern, String value) {
        if (pattern == null || pattern.isEmpty() || value == null) {
            return true;
        }
        
        // Support wildcards and multiple values separated by |
        if (pattern.equals("*")) {
            return true;
        }
        
        String[] patterns = pattern.split("\\|");
        for (String p : patterns) {
            if (p.trim().equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean matchesKeywords(String[] ruleKeywords, List<String> requestKeywords) {
        if (ruleKeywords == null || ruleKeywords.length == 0) {
            return true;
        }
        
        if (requestKeywords == null || requestKeywords.isEmpty()) {
            return false;
        }
        
        // Check if any rule keyword matches any request keyword
        return Arrays.stream(ruleKeywords)
                .anyMatch(ruleKeyword -> 
                    requestKeywords.stream()
                            .anyMatch(requestKeyword -> 
                                requestKeyword.toLowerCase().contains(ruleKeyword.toLowerCase())
                            )
                );
    }
    
    private String buildMatchReason(RoutingRule rule, RuleEvaluationRequest request) {
        StringBuilder reason = new StringBuilder("Matched rule: " + rule.getRuleName());
        
        if (rule.getIntentPattern() != null && !rule.getIntentPattern().isEmpty()) {
            reason.append(", Intent: ").append(request.getIntent());
        }
        
        if (rule.getSentimentPattern() != null && !rule.getSentimentPattern().isEmpty()) {
            reason.append(", Sentiment: ").append(request.getSentiment());
        }
        
        if (rule.getUrgencyPattern() != null && !rule.getUrgencyPattern().isEmpty()) {
            reason.append(", Urgency: ").append(request.getUrgency());
        }
        
        return reason.toString();
    }
    
    private void saveExecutionHistory(Long ruleId, Long ticketId, boolean matched, long executionTimeMs) {
        RuleExecutionHistory history = RuleExecutionHistory.builder()
                .ruleId(ruleId)
                .ticketId(ticketId)
                .matched(matched)
                .executionTimeMs(executionTimeMs)
                .build();
        
        historyRepository.save(history);
    }
}
