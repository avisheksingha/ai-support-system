package com.aisupport.routing.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.event.EventType;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.routing.dto.RoutingRuleDTO;
import com.aisupport.routing.dto.RoutingRuleRequestDTO;
import com.aisupport.routing.dto.RoutingRuleStatsDTO;
import com.aisupport.routing.dto.response.RoutingResponse;
import com.aisupport.routing.entity.RoutingRule;
import com.aisupport.routing.outbox.OutboxEventService;
import com.aisupport.routing.repository.RoutingRuleRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Evaluates routing rules for analyzed tickets and publishes routing outcomes.
 */
public class RoutingService {

    private static final String DEFAULT_USER = "admin";
    private static final String RULE_NOT_FOUND_MSG = "Routing rule not found with id: ";

    private final RuleEvaluationService ruleEvaluationService;
    private final OutboxEventService outboxService;
    private final RoutingRuleRepository routingRuleRepository;

    /**
     * Resolves assignment, priority, and SLA for a ticket analysis result,
     * then publishes a {@code TicketRoutedEvent} via outbox.
     *
     * @param event analyzed ticket event
     */
    @Transactional
    public void route(TicketAnalyzedEvent event) {

        Long ticketId = event.getTicketId();

        log.info("Routing ticketId={} using DB rules", ticketId);

        RoutingRule rule = ruleEvaluationService.evaluate(event);

        String team = rule != null ? rule.getAssignToTeam() : "general-support";
        
        TicketPriority priority = rule != null && rule.getPriorityOverride() != null
                ? rule.getPriorityOverride()
                : TicketPriority.MEDIUM;

        Integer sla = rule != null && rule.getSlaHours() != null
                ? rule.getSlaHours()
                : 24;

        TicketRoutedEvent routedEvent = TicketRoutedEvent.builder()
                .ticketId(ticketId)
                .assignToTeam(team)
                .priority(priority)
                .slaHours(sla)
                .intent(event.getAnalysis().intent())
                .sentiment(event.getAnalysis().sentiment())
                .urgency(event.getAnalysis().urgency())
                .build();

        outboxService.publishEvent(
                "TICKET",
                ticketId.toString(),
                EventType.TICKET_ROUTED,
                routedEvent
        );

        log.info("Routing completed ticketId={} team={} priority={}", ticketId, team, priority);
    }

    @Transactional
    public TicketRoutedEvent routeSync(TicketAnalyzedEvent event) {
        Long ticketId = event.getTicketId();
        log.info("Running sync routing for ticketId={}", ticketId);

        RoutingRule rule = ruleEvaluationService.evaluate(event);

        String team = rule != null ? rule.getAssignToTeam() : "general-support";
        TicketPriority priority = rule != null && rule.getPriorityOverride() != null
                ? rule.getPriorityOverride()
                : TicketPriority.MEDIUM;

        Integer sla = rule != null && rule.getSlaHours() != null
                ? rule.getSlaHours()
                : 24;

        TicketRoutedEvent routedEvent = TicketRoutedEvent.builder()
                .ticketId(ticketId)
                .assignToTeam(team)
                .priority(priority)
                .slaHours(sla)
                .intent(event.getAnalysis().intent())
                .sentiment(event.getAnalysis().sentiment())
                .urgency(event.getAnalysis().urgency())
                .build();

        log.info("Sync Routing completed ticketId={} team={} priority={}", ticketId, team, priority);
        return routedEvent;
    }
    
    /**
     * Retrieves the routing execution result for a specific ticket.
     * 
     * @param ticketId the ticket ID
     * @return RoutingResponse containing the reasoning and rule matched
     */
    @Transactional(readOnly = true)
    public RoutingResponse getRoutingForTicket(Long ticketId) {
        log.info("Fetching routing result for ticketId={}", ticketId);
        
        // Use history to find the matched rule
        return ruleEvaluationService.getMatchedExecution(ticketId)
            .map(history -> {
                RoutingRule rule = ruleEvaluationService.getRuleById(history.getRuleId());
                
                return RoutingResponse.builder()
                    .id(history.getId())
                    .ticketId(ticketId)
                    .department(rule != null ? rule.getAssignToTeam() : "General Support")
                    .confidenceScore(0.95) // High confidence for deterministic rules
                    .reason(rule != null ? rule.getDescription() : "Fallback routing")
                    .ruleName(rule != null ? rule.getRuleName() : "FALLBACK_RULE")
                    .ruleVersion(rule != null ? rule.getRuleVersion() : 1)
                    .executedAt(history.getExecutedAt())
                    .build();
            })
            .orElseGet(() -> RoutingResponse.builder()
                .ticketId(ticketId)
                .department("General Support")
                .confidenceScore(0.5)
                .reason("No routing rule matched.")
                .build());
    }

    /* ---------------- Admin Rule Management Methods ---------------- */

    @Transactional(readOnly = true)
    public Page<RoutingRuleDTO> searchRules(String search, Boolean active, String team, Pageable pageable) {
        Specification<RoutingRule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("ruleName")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(cb.coalesce(root.get("description"), "")), searchPattern);
                Predicate teamMatch = cb.like(cb.lower(root.get("assignToTeam")), searchPattern);
                predicates.add(cb.or(nameMatch, descMatch, teamMatch));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (team != null && !team.trim().isEmpty() && !"ALL".equalsIgnoreCase(team)) {
                predicates.add(cb.equal(cb.lower(root.get("assignToTeam")), team.trim().toLowerCase()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return routingRuleRepository.findAll(spec, pageable)
                .map(RoutingRuleDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public RoutingRuleDTO getRuleDTOById(Long id) {
        return routingRuleRepository.findById(id)
                .map(RoutingRuleDTO::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException(RULE_NOT_FOUND_MSG + id));
    }

    @Transactional
    public RoutingRuleDTO createRule(RoutingRuleRequestDTO request, String username) {
        log.info("Admin {} creating routing rule: {}", username, request.getRuleName());

        if (routingRuleRepository.existsByRuleName(request.getRuleName().trim())) {
            throw new IllegalArgumentException("Routing rule with name '" + request.getRuleName().trim() + "' already exists");
        }

        RoutingRule rule = RoutingRule.builder()
                .ruleName(request.getRuleName().trim())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .active(!Boolean.FALSE.equals(request.getActive()))
                .intentPattern(request.getIntentPattern())
                .sentimentPattern(request.getSentimentPattern())
                .urgencyPattern(request.getUrgencyPattern())
                .keywordPatterns(request.getKeywordPatterns() != null ? request.getKeywordPatterns().toArray(new String[0]) : new String[0])
                .assignToTeam(request.getAssignToTeam().trim())
                .priorityOverride(request.getPriorityOverride())
                .slaHours(request.getSlaHours() != null ? request.getSlaHours() : 24)
                .ruleVersion(1)
                .createdBy(username != null ? username : DEFAULT_USER)
                .updatedBy(username != null ? username : DEFAULT_USER)
                .build();

        RoutingRule saved = routingRuleRepository.save(rule);
        log.info("Routing rule created with ID: {}", saved.getId());
        return RoutingRuleDTO.fromEntity(saved);
    }

    @Transactional
    public RoutingRuleDTO updateRule(Long id, RoutingRuleRequestDTO request, String username) {
        log.info("Admin {} updating routing rule ID: {}", username, id);

        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(RULE_NOT_FOUND_MSG + id));

        String newName = request.getRuleName().trim();
        if (!rule.getRuleName().equalsIgnoreCase(newName) && routingRuleRepository.existsByRuleName(newName)) {
            throw new IllegalArgumentException("Routing rule with name '" + newName + "' already exists");
        }

        rule.setRuleName(newName);
        rule.setDescription(request.getDescription());
        rule.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        if (request.getActive() != null) {
            rule.setActive(request.getActive());
        }
        rule.setIntentPattern(request.getIntentPattern());
        rule.setSentimentPattern(request.getSentimentPattern());
        rule.setUrgencyPattern(request.getUrgencyPattern());
        rule.setKeywordPatterns(request.getKeywordPatterns() != null ? request.getKeywordPatterns().toArray(new String[0]) : new String[0]);
        rule.setAssignToTeam(request.getAssignToTeam().trim());
        rule.setPriorityOverride(request.getPriorityOverride());
        rule.setSlaHours(request.getSlaHours() != null ? request.getSlaHours() : 24);
        rule.setRuleVersion(rule.getRuleVersion() != null ? rule.getRuleVersion() + 1 : 1);
        rule.setUpdatedBy(username != null ? username : DEFAULT_USER);

        RoutingRule saved = routingRuleRepository.save(rule);
        log.info("Routing rule ID: {} updated to version: {}", saved.getId(), saved.getRuleVersion());
        return RoutingRuleDTO.fromEntity(saved);
    }

    @Transactional
    public RoutingRuleDTO toggleRuleActive(Long id, String username) {
        log.info("Admin {} toggling active state for rule ID: {}", username, id);

        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(RULE_NOT_FOUND_MSG + id));

        rule.setActive(!Boolean.TRUE.equals(rule.getActive()));
        rule.setUpdatedBy(username != null ? username : DEFAULT_USER);

        RoutingRule saved = routingRuleRepository.save(rule);
        log.info("Rule ID: {} active status toggled to: {}", saved.getId(), saved.getActive());
        return RoutingRuleDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteRule(Long id) {
        log.info("Deleting routing rule ID: {}", id);
        if (!routingRuleRepository.existsById(id)) {
            throw new IllegalArgumentException(RULE_NOT_FOUND_MSG + id);
        }
        routingRuleRepository.deleteById(id);
        log.info("Routing rule ID: {} deleted successfully", id);
    }

    @Transactional(readOnly = true)
    public RoutingRuleStatsDTO getRuleStats() {
        long total = routingRuleRepository.count();
        Long active = routingRuleRepository.countByActive(true);
        Long inactive = routingRuleRepository.countByActive(false);
        java.util.List<String> teams = routingRuleRepository.findDistinctAssignToTeams();

        return RoutingRuleStatsDTO.builder()
                .totalRules(total)
                .activeRules(active != null ? active : 0)
                .inactiveRules(inactive != null ? inactive : 0)
                .totalTeamsCount(teams != null ? teams.size() : 0)
                .targetTeams(teams != null ? teams : java.util.Collections.emptyList())
                .build();
    }
}
