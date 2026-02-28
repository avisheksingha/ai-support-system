package com.aisupport.rule.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.rule.dto.RuleRequest;
import com.aisupport.rule.dto.RuleResponse;
import com.aisupport.rule.entity.RoutingRule;
import com.aisupport.rule.exception.RuleNotFoundException;
import com.aisupport.rule.mapper.RoutingRuleMapper;
import com.aisupport.rule.repository.RoutingRuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleService {

    private final RoutingRuleRepository routingRuleRepository;
    private final RoutingRuleMapper routingRuleMapper;

    @Transactional
    public RuleResponse createRule(RuleRequest request) {
        log.info("Creating new routing rule: {}", request.getRuleName());

        if (routingRuleRepository.existsByRuleName(request.getRuleName())) {
            throw new IllegalArgumentException("Rule with name '" + request.getRuleName() + "' already exists");
        }

        RoutingRule rule = routingRuleMapper.toEntity(request);
        // Apply defaults for optional fields not set by the mapper
        if (rule.getPriority() == null) rule.setPriority(0);
        if (rule.getActive() == null) rule.setActive(true);

        rule = routingRuleRepository.save(rule);
        log.info("Rule created successfully with ID: {}", rule.getId());

        return routingRuleMapper.toResponse(rule);
    }

    @Transactional(readOnly = true)
    public RuleResponse getRuleById(Long id) {
        log.info("Fetching rule by ID: {}", id);
        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with ID: " + id));
        return routingRuleMapper.toResponse(rule);
    }

    @Transactional(readOnly = true)
    public RuleResponse getRuleByName(String ruleName) {
        log.info("Fetching rule by name: {}", ruleName);
        RoutingRule rule = routingRuleRepository.findByRuleName(ruleName)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with name: " + ruleName));
        return routingRuleMapper.toResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<RuleResponse> getActiveRules() {
        log.info("Fetching all active rules");
        return routingRuleRepository.findActiveRulesOrderedByPriority().stream()
                .map(routingRuleMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RuleResponse> getAllRules() {
        log.info("Fetching all rules");
        return routingRuleRepository.findAll().stream()
                .map(routingRuleMapper::toResponse)
                .toList();
    }

    @Transactional
    public RuleResponse updateRule(Long id, RuleRequest request) {
        log.info("Updating rule with ID: {}", id);

        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with ID: " + id));

        // Check if renaming and new name already exists
        if (!rule.getRuleName().equals(request.getRuleName()) &&
            routingRuleRepository.existsByRuleName(request.getRuleName())) {
            throw new IllegalArgumentException("Rule with name '" + request.getRuleName() + "' already exists");
        }

        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setPriority(request.getPriority() != null ? request.getPriority() : rule.getPriority());
        rule.setActive(request.getActive() != null ? request.getActive() : rule.getActive());
        rule.setIntentPattern(request.getIntentPattern());
        rule.setSentimentPattern(request.getSentimentPattern());
        rule.setUrgencyPattern(request.getUrgencyPattern());
        rule.setKeywordPatterns(request.getKeywordPatterns() != null ?
                request.getKeywordPatterns().toArray(new String[0]) : rule.getKeywordPatterns());
        rule.setAssignToTeam(request.getAssignToTeam());
        rule.setPriorityOverride(request.getPriorityOverride());
        rule.setSlaHours(request.getSlaHours());

        rule = routingRuleRepository.save(rule);
        log.info("Rule updated successfully: {}", rule.getRuleName());

        return routingRuleMapper.toResponse(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        log.info("Deleting rule with ID: {}", id);

        if (!routingRuleRepository.existsById(id)) {
            throw new RuleNotFoundException("Rule not found with ID: " + id);
        }

        routingRuleRepository.deleteById(id);
        log.info("Rule deleted successfully");
    }

    @Transactional
    public RuleResponse toggleRuleStatus(Long id) {
        log.info("Toggling status for rule with ID: {}", id);

        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with ID: " + id));

        rule.setActive(!rule.getActive());
        rule = routingRuleRepository.save(rule);

        log.info("Rule status toggled to: {}", rule.getActive());
        return routingRuleMapper.toResponse(rule);
    }
}
