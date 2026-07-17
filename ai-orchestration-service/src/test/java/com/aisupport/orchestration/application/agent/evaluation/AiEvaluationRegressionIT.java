package com.aisupport.orchestration.application.agent.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.aisupport.orchestration.application.workflow.steps.FinalAiDecisionStep;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.infrastructure.AbstractIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class AiEvaluationRegressionIT extends AbstractIntegrationTest {

    @Autowired
    private FinalAiDecisionStep finalAiDecisionStep;

    @MockitoBean(name = "googleGenAiChatModel")
    private ChatModel chatModel;

    @BeforeEach
    void setupChatClient() {
        // ChatClient uses ChatModel, so we mock ChatModel to control responses
        // and capture prompts.
    }

    static List<AiRegressionScenario> provideScenarios() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = AiEvaluationRegressionIT.class.getResourceAsStream("/datasets/ai_regression_scenarios.json")) {
            return mapper.readValue(is, new TypeReference<List<AiRegressionScenario>>() {});
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideScenarios")
    void testAiRegressionScenarios(AiRegressionScenario scenario) {
        log.info("Running AI Evaluation Scenario: {}", scenario.getScenarioName());

        // 1. Prepare WorkflowContext
        WorkflowContext context = WorkflowContext.builder()
                .ticketId(scenario.getTicketId())
                .executionId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .build();
        
        // (If the scenario included actual conversation/knowledge context, we'd hydrate the WorkflowContext here)

        // 2. Mock LLM Response
        Generation generation = new Generation(new AssistantMessage(scenario.getMockLlmResponse()));
        ChatResponse mockResponse = new ChatResponse(List.of(generation));
        
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        when(chatModel.call(promptCaptor.capture())).thenReturn(mockResponse);

        // 3. Execute Step
        try {
            finalAiDecisionStep.execute(context);
        } catch (Exception e) {
            throw new AssertionError("Unexpected failure: " + e.getMessage(), e);
        }

        com.aisupport.common.event.AiDecision decision = context.getResource(com.aisupport.common.event.AiDecision.class);

        // 4. Verify Policy Block Expectation
        if (scenario.isExpectPolicyBlock()) {
            if (decision == null || !decision.aiSummary().contains("blocked")) {
                throw new AssertionError("Expected policy block but execution succeeded.");
            }
            return; // Expected block
        } else if (decision != null && decision.aiSummary().contains("blocked")) {
            throw new AssertionError("Unexpected policy block.");
        }

        // 5. Verify Prompt Snapshot
        Prompt capturedPrompt = promptCaptor.getValue();
        String promptText = capturedPrompt.getContents();
        assertThat(promptText).contains(scenario.getExpectedPromptSnippet());
    }
}
