package com.aisupport.routing.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.routing.entity.RoutingRule;
import com.aisupport.routing.entity.RuleExecutionHistory;
import com.aisupport.routing.repository.RoutingRuleRepository;
import com.aisupport.routing.repository.RuleExecutionHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluationService {

    private final RoutingRuleRepository routingRuleRepository;
    private final RuleExecutionHistoryRepository historyRepository;

    @Transactional
    public RoutingRule evaluate(TicketAnalyzedEvent event) {

        Long ticketId = event.getTicketId();

        List<RoutingRule> rules =
                routingRuleRepository.findActiveRulesOrderedByPriority();

        log.debug("Evaluating {} rules for ticketId={}", rules.size(), ticketId);

        for (RoutingRule rule : rules) {

            long ruleStart = System.nanoTime();
            boolean matches = evaluateRule(rule, event);
            long execTime = System.nanoTime() - ruleStart;

            saveExecutionHistory(rule.getId(), ticketId, matches, execTime);

            if (matches) {
                log.info("Matched rule={} for ticketId={}",
                        rule.getRuleName(), ticketId);

                return rule;
            }
        }

        log.warn("No rule matched for ticketId={}, using fallback", ticketId);
        return null;
    }

    /* ---------------- MATCH LOGIC ---------------- */
    
    private boolean evaluateRule(RoutingRule rule, TicketAnalyzedEvent e) {
        
        boolean isKeywordMatch = rule.getKeywordPatterns() == null 
                || rule.getKeywordPatterns().length == 0 
                || matchKeywords(rule.getKeywordPatterns(), e.getKeywords());

        return match(rule.getIntentPattern(), e.getIntent()) 
                && match(rule.getSentimentPattern(), e.getSentiment()) 
                && match(rule.getUrgencyPattern(), e.getUrgency()) 
                && isKeywordMatch;
    }

    private boolean match(String pattern, String value) {

        if (pattern == null || pattern.isBlank()) return true;
        if (value == null) return false;

        if (pattern.equals("*")) return true;

        String[] parts = pattern.split("\\|");

        for (String p : parts) {
            if (p.trim().equalsIgnoreCase(value.trim())) return true;
        }

        return false;
    }

    private boolean matchKeywords(String[] ruleKeywords, List<String> requestKeywords) {

        if (requestKeywords == null || requestKeywords.isEmpty()) return false;

        return Arrays.stream(ruleKeywords)
            .anyMatch(ruleKeyword ->
                requestKeywords.stream()
                    .anyMatch(req ->
                        req.toLowerCase()
                        	.contains(ruleKeyword.toLowerCase())
                    )
            );
    }

    private void saveExecutionHistory(Long ruleId, Long ticketId, boolean matched, long executionTimeNs) {

        RuleExecutionHistory history = RuleExecutionHistory.builder()
                .ruleId(ruleId)
                .ticketId(ticketId)
                .matched(matched)
                .executionTimeNs(executionTimeNs)
                .build();

        historyRepository.save(history);
    }
    
    public Optional<RuleExecutionHistory> getMatchedExecution(Long ticketId) {
        return historyRepository.findTopByTicketIdAndMatchedTrueOrderByExecutedAtDesc(ticketId);
    }
    
    public RoutingRule getRuleById(Long ruleId) {
        return routingRuleRepository.findById(ruleId).orElse(null);
    }
}