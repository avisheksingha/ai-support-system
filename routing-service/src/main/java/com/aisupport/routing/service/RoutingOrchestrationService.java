package com.aisupport.routing.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aisupport.common.dto.AnalysisResultDTO;
import com.aisupport.common.dto.TicketDTO;
import com.aisupport.routing.client.AIAnalysisServiceClient;
import com.aisupport.routing.client.RuleEngineServiceClient;
import com.aisupport.routing.client.TicketServiceClient;
import com.aisupport.routing.dto.RoutingRequest;
import com.aisupport.routing.dto.RoutingResponse;
import com.aisupport.routing.dto.RuleEvaluationRequest;
import com.aisupport.routing.dto.RuleEvaluationResponse;
import com.aisupport.routing.exception.RoutingException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingOrchestrationService {
    
    private final TicketServiceClient ticketServiceClient;
    private final AIAnalysisServiceClient aiAnalysisServiceClient;
    private final RuleEngineServiceClient ruleEngineServiceClient;
    
    @Value("${routing.fallback.enabled:true}")
    private Boolean fallbackEnabled;
    
    @Value("${routing.fallback.default-team:general-support}")
    private String defaultTeam;
    
    @Value("${routing.fallback.default-priority:MEDIUM}")
    private String defaultPriority;
    
    @Value("${rule.fallback.default-sla-hours:24}")
    private Integer defaultSlaHours;
    
    public RoutingResponse routeTicket(RoutingRequest request) {
        log.info("Starting routing workflow for ticket ID: {}", request.getTicketId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Fetch ticket details
            TicketDTO ticket = fetchTicket(request.getTicketId());
            
            // Step 2: Get AI analysis
            AnalysisResultDTO analysis = getAnalysis(request.getTicketId());
            
            // Step 3: Evaluate routing rules
            RuleEvaluationResponse ruleEvaluation = evaluateRules(request.getTicketId(), analysis);
            
            // Step 4: Apply routing decision
            applyRoutingDecision(ticket, ruleEvaluation);
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            log.info("Routing completed successfully for ticket {} in {}ms", 
                    request.getTicketId(), totalTime);
            
            return buildSuccessResponse(ticket, analysis, ruleEvaluation, totalTime);
            
        } catch (Exception e) {
            log.error("Routing workflow failed for ticket {}", request.getTicketId(), e);
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            if (fallbackEnabled) {
                return handleFallbackRouting(request.getTicketId(), e, totalTime);
            } else {
                throw new RoutingException("Routing workflow failed: " + e.getMessage(), e);
            }
        }
    }
    
    @CircuitBreaker(name = "ticketService", fallbackMethod = "fetchTicketFallback")
    @Retry(name = "ticketService")
    private TicketDTO fetchTicket(Long ticketId) {
        long stepStart = System.currentTimeMillis();
        
        try {
            log.debug("Fetching ticket details for ID: {}", ticketId);
            TicketDTO ticket = ticketServiceClient.getTicketById(ticketId);
            
            long stepDuration = System.currentTimeMillis() - stepStart;
            log.debug("Fetched ticket in {}ms", stepDuration);
            
            return ticket;
            
        } catch (Exception e) {
            long stepDuration = System.currentTimeMillis() - stepStart;
            log.error("Failed to fetch ticket {} after {}ms", ticketId, stepDuration);
            throw e;
        }
    }
    
    @CircuitBreaker(name = "aiAnalysisService", fallbackMethod = "getAnalysisFallback")
    @Retry(name = "aiAnalysisService")
    private AnalysisResultDTO getAnalysis(Long ticketId) {
        long stepStart = System.currentTimeMillis();
        
        try {
            log.debug("Fetching AI analysis for ticket ID: {}", ticketId);
            AnalysisResultDTO analysis = aiAnalysisServiceClient.getAnalysisByTicketId(ticketId);
            
            if (analysis == null) {
                throw new RoutingException("No analysis found for ticket: " + ticketId);
            }
            
            long stepDuration = System.currentTimeMillis() - stepStart;
            log.debug("Fetched analysis in {}ms", stepDuration);
            
            return analysis;
            
        } catch (Exception e) {
            long stepDuration = System.currentTimeMillis() - stepStart;
            log.error("Failed to fetch analysis for ticket {} after {}ms", ticketId, stepDuration);
            throw e;
        }
    }
    
    @CircuitBreaker(name = "ruleEngineService", fallbackMethod = "evaluateRulesFallback")
    @Retry(name = "ruleEngineService")
    private RuleEvaluationResponse evaluateRules(Long ticketId, AnalysisResultDTO analysis) {
        long stepStart = System.currentTimeMillis();
        
        try {
            log.debug("Evaluating routing rules for ticket ID: {}", ticketId);
            
            RuleEvaluationRequest request = RuleEvaluationRequest.builder()
                    .ticketId(ticketId)
                    .intent(analysis.getIntent())
                    .sentiment(analysis.getSentiment())
                    .urgency(analysis.getUrgency())
                    .keywords(analysis.getKeywords())
                    .build();
            
            RuleEvaluationResponse evaluation = ruleEngineServiceClient.evaluateRules(request);
            
            long stepDuration = System.currentTimeMillis() - stepStart;
            log.debug("Evaluated rules in {}ms", stepDuration);
            
            return evaluation;
            
        } catch (Exception e) {
            long stepDuration = System.currentTimeMillis() - stepStart;
            log.error("Failed to evaluate rules for ticket {} after {}ms", ticketId, stepDuration);
            throw e;
        }
    }
    
    @CircuitBreaker(name = "ticketService", fallbackMethod = "applyRoutingFallback")
    @Retry(name = "ticketService")
    private void applyRoutingDecision(TicketDTO ticket, RuleEvaluationResponse evaluation) {
        long stepStart = System.currentTimeMillis();
        
        try {
            log.debug("Applying routing decision for ticket: {}", ticket.getTicketNumber());
            
            // Assign to team
            ticketServiceClient.assignTicket(
                    ticket.getTicketNumber(), 
                    evaluation.getAssignToTeam());
            
            // Update priority if override exists
            if (evaluation.getPriorityOverride() != null) {
                ticketServiceClient.updatePriority(
                        ticket.getTicketNumber(), 
                        evaluation.getPriorityOverride());
            }
            
            long stepDuration = System.currentTimeMillis() - stepStart;
            log.debug("Applied routing in {}ms", stepDuration);
            
        } catch (Exception e) {
            long stepDuration = System.currentTimeMillis() - stepStart;
            log.error("Failed to apply routing for ticket {} after {}ms", 
                    ticket.getTicketNumber(), stepDuration);
            throw e;
        }
    }
    
    private RoutingResponse buildSuccessResponse(TicketDTO ticket,
                                                 AnalysisResultDTO analysis,
                                                 RuleEvaluationResponse evaluation,
                                                 long processingTimeMs) {
        return RoutingResponse.builder()
                .ticketId(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .assignedTo(evaluation.getAssignToTeam())
                .priority(evaluation.getPriorityOverride() != null ? 
                        evaluation.getPriorityOverride() : ticket.getPriority())
                .slaHours(evaluation.getSlaHours())
                .intent(analysis.getIntent())
                .sentiment(analysis.getSentiment())
                .urgency(analysis.getUrgency())
                .matchedRule(evaluation.getMatchedRuleName())
                .routingReason(evaluation.getReason())
                .success(true)
                .processingTimeMs(processingTimeMs)
                .routedAt(LocalDateTime.now())
                .build();
    }
    
    private RoutingResponse handleFallbackRouting(Long ticketId, Exception error, long processingTime) {
        log.warn("Using fallback routing for ticket {}: {}", ticketId, error.getMessage());
        
        try {
            TicketDTO ticket = ticketServiceClient.getTicketById(ticketId);
            
            // Apply default routing
            ticketServiceClient.assignTicket(ticket.getTicketNumber(), defaultTeam);
            ticketServiceClient.updatePriority(ticket.getTicketNumber(), defaultPriority);
            
            return RoutingResponse.builder()
                    .ticketId(ticketId)
                    .ticketNumber(ticket.getTicketNumber())
                    .assignedTo(defaultTeam)
                    .priority(defaultPriority)
                    .slaHours(defaultSlaHours)
                    .matchedRule("Fallback Routing")
                    .routingReason("Workflow failed, using default routing: " + error.getMessage())
                    .success(true)
                    .processingTimeMs(processingTime)
                    .routedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception fallbackError) {
            log.error("Fallback routing also failed for ticket {}", ticketId, fallbackError);
            
            return RoutingResponse.builder()
                    .ticketId(ticketId)
                    .success(false)
                    .errorMessage("Complete routing failure: " + fallbackError.getMessage())
                    .processingTimeMs(processingTime)
                    .build();
        }
    }
    
    // Fallback methods for circuit breaker
    private TicketDTO fetchTicketFallback(Long ticketId, Exception e) {
        log.error("Circuit breaker: Failed to fetch ticket {}", ticketId, e);
        throw new RoutingException("Ticket service unavailable", e);
    }
    
    private AnalysisResultDTO getAnalysisFallback(Long ticketId, Exception e) {
        log.error("Circuit breaker: Failed to get analysis for ticket {}", ticketId, e);
        throw new RoutingException("AI Analysis service unavailable", e);
    }
    
    private RuleEvaluationResponse evaluateRulesFallback(Long ticketId, 
                                                         AnalysisResultDTO analysis,
                                                         Exception e) {
        log.error("Circuit breaker: Failed to evaluate rules for ticket {}", ticketId, e);
        throw new RoutingException("Rule Engine service unavailable", e);
    }
    
    private void applyRoutingFallback(TicketDTO ticket, 
                                     RuleEvaluationResponse evaluation,
                                     Exception e) {
        log.error("Circuit breaker: Failed to apply routing for ticket {}", 
                ticket.getTicketNumber(), e);
        throw new RoutingException("Failed to apply routing decision", e);
    }
}