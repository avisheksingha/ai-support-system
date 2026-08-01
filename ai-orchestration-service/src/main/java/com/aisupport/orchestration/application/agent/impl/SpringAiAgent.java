package com.aisupport.orchestration.application.agent.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.Agent;
import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.agent.AgentResponse;
import com.aisupport.orchestration.application.agent.AgentSession;
import com.aisupport.orchestration.application.agent.budget.TokenBudgetManager;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailContext;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailPipeline;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailResult;
import com.aisupport.orchestration.domain.model.Result;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("springAiAgent")
public class SpringAiAgent implements Agent {

    private static final String UNKNOWN_MODEL = "Unknown";

    private final TokenBudgetManager tokenBudgetManager;
    private final GuardrailPipeline guardrailPipeline;
    private final ChatModel chatModel;

    public SpringAiAgent(TokenBudgetManager tokenBudgetManager,
                         GuardrailPipeline guardrailPipeline,
                         @Qualifier("googleGenAiChatModel") ChatModel chatModel) {
        this.tokenBudgetManager = tokenBudgetManager;
        this.guardrailPipeline = guardrailPipeline;
        this.chatModel = chatModel;
    }

    @Override
    public Result<AgentSession> execute(AgentRequest request) {
        log.info("SpringAiAgent executing inference for model: {}", 
                 request.getModelProfile() != null ? request.getModelProfile().getName() : UNKNOWN_MODEL);
                 
        AgentSession session = AgentSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .initialRequest(request)
                .startedAt(Instant.now())
                .build();
        
        // 1. Guardrails (Input)
        GuardrailContext<AgentRequest> inputContext = GuardrailContext.<AgentRequest>builder()
                .payload(request)
                .metadata(new HashMap<>())
                .build();
        
        GuardrailResult<AgentRequest> inputResult = guardrailPipeline.runInputGuardrails(inputContext);
        if (inputResult.getStatus() == GuardrailResult.Status.BLOCK) {
             session.setFailureReason(inputResult.getReason());
             session.setCompletedAt(Instant.now());

             return Result.success(session);
        }
        
        AgentRequest safeRequest = inputResult.getPayload();

        // 2. Enforce Token Budget
        AgentRequest budgetedRequest = tokenBudgetManager.applyBudget(safeRequest);
        
        // 3. LLM Inference
        log.debug("Calling LLM API...");
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(budgetedRequest.getSystemPrompt()),
                new UserMessage(budgetedRequest.getUserPrompt())
        ));
        
        ChatResponse chatResponse = chatModel.call(prompt);
        
        Usage usage = chatResponse.getMetadata() != null ? chatResponse.getMetadata().getUsage() : null;
        int promptTokens = usage != null && usage.getPromptTokens() != null ? usage.getPromptTokens().intValue() : 0;
        int totalTokens = usage != null && usage.getTotalTokens() != null ? usage.getTotalTokens().intValue() : 0;
        int completionTokens = Math.max(0, totalTokens - promptTokens);
        
        AgentResponse response = AgentResponse.builder()
                .responseType(AgentResponse.ResponseType.FINAL)
                .content(chatResponse.getResult().getOutput().getText())
                .finishReason(AgentResponse.FinishReason.STOP)
                .usage(AgentResponse.UsageData.builder()
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .totalTokens(totalTokens)
                        .build())
                .metadata(new HashMap<>())
                .build();
                
        // 4. Guardrails (Output)
        GuardrailContext<AgentResponse> outputContext = GuardrailContext.<AgentResponse>builder()
                .payload(response)
                .metadata(new HashMap<>())
                .build();
        
        GuardrailResult<AgentResponse> outputResult = guardrailPipeline.runOutputGuardrails(outputContext);
        
        if (outputResult.getStatus() == GuardrailResult.Status.BLOCK) {
            session.setFailureReason(outputResult.getReason());
            session.setCompletedAt(Instant.now());
            return Result.success(session);
        }
                
        session.complete(outputResult.getPayload());
        return Result.success(session);
    }
}
