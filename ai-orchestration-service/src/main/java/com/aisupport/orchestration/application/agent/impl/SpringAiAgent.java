package com.aisupport.orchestration.application.agent.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.Agent;
import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.agent.AgentResponse;
import com.aisupport.orchestration.application.agent.AgentSession;
import com.aisupport.orchestration.application.agent.budget.TokenBudgetManager;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailContext;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailPipeline;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailResult;
import com.aisupport.orchestration.application.tool.ToolExecutor;
import com.aisupport.orchestration.domain.model.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiAgent implements Agent {

    private final TokenBudgetManager tokenBudgetManager;
    private final ToolExecutor toolExecutor;
    private final GuardrailPipeline guardrailPipeline;

    @Override
    public Result<AgentSession> execute(AgentRequest request) {
        log.info("SpringAiAgent executing inference for model: {}", 
                 request.getModelProfile() != null ? request.getModelProfile().getName() : "unknown");
                 
        AgentSession session = AgentSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .initialRequest(request)
                .startedAt(Instant.now())
                .build();
        
        // 0. Guardrails (Input)
        GuardrailContext<AgentRequest> inputContext = GuardrailContext.<AgentRequest>builder()
                .payload(request)
                .metadata(new HashMap<>())
                .build();
        GuardrailResult<AgentRequest> inputResult = guardrailPipeline.runInputGuardrails(inputContext);
        if (inputResult.getStatus() == GuardrailResult.Status.BLOCK) {
             session.setFailureReason(inputResult.getReason());
             session.setCompletedAt(Instant.now());
             // For V1 portfolio we just use a placeholder ID and version based on the reason
             session.setGuardrailId(inputResult.getReason().split(":")[0].toLowerCase());
             session.setGuardrailVersion("1.0");
             return Result.success(session);
        }
        AgentRequest safeRequest = inputResult.getPayload();

        // 1. Enforce Token Budget
        AgentRequest budgetedRequest = tokenBudgetManager.applyBudget(safeRequest);
        
        // 2. Initial Inference
        log.debug("Calling LLM API...");
        
        // Pseudo-logic simulating a tool call from the LLM
        boolean llmRequestedTool = budgetedRequest.getAllowedCapabilities() != null 
                                && !budgetedRequest.getAllowedCapabilities().isEmpty();
                                
        if (llmRequestedTool) {
            log.info("LLM requested tool execution. Intercepting...");
            
            // 3. Delegate to ToolExecutor
            String requestedTool = budgetedRequest.getAllowedCapabilities().get(0);
            log.debug("Executing tool: {}", requestedTool);
            
            try {
                Object toolResult = toolExecutor.execute(requestedTool, new java.util.HashMap<>());
                log.debug("Tool executed successfully. Result: {}", toolResult);
                
                // 4. Feed Tool Result back to LLM for final reasoning
                log.debug("Feeding tool result back to LLM...");
            } catch (Exception e) {
                log.error("Tool execution failed", e);
                return Result.failure(e.getMessage());
            }
        }
        
        // 5. Final LLM Response
        AgentResponse response = AgentResponse.builder()
                .responseType(AgentResponse.ResponseType.FINAL)
                .content("AI inference completed after reasoning.")
                .finishReason(AgentResponse.FinishReason.STOP)
                .usage(AgentResponse.UsageData.builder()
                        .promptTokens(100)
                        .completionTokens(50)
                        .totalTokens(150)
                        .build())
                .metadata(new HashMap<>())
                .build();
                
        // 6. Guardrails (Output)
        GuardrailContext<AgentResponse> outputContext = GuardrailContext.<AgentResponse>builder()
                .payload(response)
                .metadata(new HashMap<>())
                .build();
        GuardrailResult<AgentResponse> outputResult = guardrailPipeline.runOutputGuardrails(outputContext);
        if (outputResult.getStatus() == GuardrailResult.Status.BLOCK) {
            session.setFailureReason(outputResult.getReason());
            session.setGuardrailId(outputResult.getReason().split(":")[0].toLowerCase());
            session.setGuardrailVersion("1.0");
            session.setCompletedAt(Instant.now());
            return Result.success(session);
        }
                
        session.complete(outputResult.getPayload());
        return Result.success(session);
    }
}
