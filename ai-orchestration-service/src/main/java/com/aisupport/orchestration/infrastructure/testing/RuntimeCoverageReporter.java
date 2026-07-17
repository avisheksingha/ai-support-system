package com.aisupport.orchestration.infrastructure.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.workflow.WorkflowExecutionListener;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(38)
@RequiredArgsConstructor
public class RuntimeCoverageReporter implements WorkflowExecutionListener {

    private static final String REPORT_PATH = "target/runtime-coverage.md";
    
    private final WorkflowExecutionRepository workflowRepository;
    private final AiExecutionRecordRepository aiRecordRepository;

    @Override
    public void beforeWorkflow(WorkflowContext context) { 
        // No action needed before workflow starts
    }

    @Override
    public void afterWorkflow(WorkflowContext context) {
        generateReport(context, "COMPLETED");
    }

    @Override
    public void onFailure(WorkflowContext context, Throwable cause) {
        generateReport(context, "FAILED");
    }

    private void generateReport(WorkflowContext context, String finalStatus) {
        StringBuilder report = new StringBuilder();
        
        report.append("# Runtime Coverage Report\n\n");
        report.append("## Workflow Execution\n");
        report.append("- **Workflow ID**: ").append(context.getWorkflowId()).append("\n");
        report.append("- **Execution ID**: ").append(context.getExecutionId()).append("\n");
        report.append("- **Correlation ID**: ").append(context.getCorrelationId()).append("\n");
        report.append("- **Ticket ID**: ").append(context.getTicketId()).append("\n");
        report.append("- **Final Status**: ").append(finalStatus).append("\n\n");

        appendWorkflowExecutionDetails(context, report);
        appendAiExecutionDetails(context, report);

        String reportContent = report.toString();
        
        log.info("\n========== RUNTIME COVERAGE ==========\n{}\n======================================", reportContent);
        
        try {
            Path path = Paths.get(REPORT_PATH);
            Files.createDirectories(path.getParent());
            Files.writeString(path, reportContent);
        } catch (IOException e) {
            log.error("Failed to write runtime coverage report to {}", REPORT_PATH, e);
        }
    }

    private void appendWorkflowExecutionDetails(WorkflowContext context, StringBuilder report) {
        WorkflowExecutionEntity workflowExecution = workflowRepository.findById(context.getExecutionId()).orElse(null);
        if (workflowExecution != null) {
            report.append("## Status Details\n");
            report.append("- **Workflow Status**: ").append(workflowExecution.getState()).append("\n");
            report.append("- **Service Version**: 1.0.0\n");
            report.append("- **Execution Time**: ").append(context.getExecutionDuration()).append(" ms\n");
            report.append("- **Workflow Resume Count**: ").append(workflowExecution.getRecoveryCount()).append("\n");
            report.append("- **Checkpoint Used**: ").append(workflowExecution.getCurrentStep() != null ? workflowExecution.getCurrentStep() : "No").append("\n");
            report.append("\n");
        }
    }

    private void appendAiExecutionDetails(WorkflowContext context, StringBuilder report) {
        List<AiExecutionRecordEntity> aiRecords = aiRecordRepository.findByCorrelationId(context.getCorrelationId());
        if (aiRecords != null && !aiRecords.isEmpty()) {
            report.append("## AI Governance & Execution\n");
            for (AiExecutionRecordEntity aiRecord : aiRecords) {
                if ("AGENT".equals(aiRecord.getRecordType())) {
                    appendSingleAiRecord(report, aiRecord);
                }
            }
        }
    }

    private void appendSingleAiRecord(StringBuilder report, AiExecutionRecordEntity aiRecord) {
        report.append("### Agent Execution (").append(aiRecord.getOutcome()).append(")\n");

        report.append("- **Model**: ").append(aiRecord.getModelId()).append("\n");
        
        int toolCount = 0;
        if (aiRecord.getToolsInvoked() != null && !aiRecord.getToolsInvoked().isEmpty() && !aiRecord.getToolsInvoked().equals("None")) {
            toolCount = aiRecord.getToolsInvoked().split(",").length;
        }
        report.append("- **Tools Used**: ").append(aiRecord.getToolsInvoked() != null && !aiRecord.getToolsInvoked().isEmpty() ? aiRecord.getToolsInvoked() : "None").append(" (Count: ").append(toolCount).append(")\n");
        
        report.append("- **Prompt Tokens**: ").append(aiRecord.getPromptTokens() != null ? aiRecord.getPromptTokens() : 0).append("\n");
        report.append("- **Completion Tokens**: ").append(aiRecord.getCompletionTokens() != null ? aiRecord.getCompletionTokens() : 0).append("\n");
        report.append("- **Total Tokens**: ").append((aiRecord.getPromptTokens() != null ? aiRecord.getPromptTokens() : 0) + (aiRecord.getCompletionTokens() != null ? aiRecord.getCompletionTokens() : 0)).append("\n");
        
        report.append("- **Policy**: ").append(aiRecord.getPolicyId() != null ? aiRecord.getPolicyId() + " (v" + aiRecord.getPolicyVersion() + ")" : "None").append("\n");
        report.append("- **Guardrail**: ").append(aiRecord.getGuardrailId() != null ? aiRecord.getGuardrailId() + " (v" + aiRecord.getGuardrailVersion() + ")" : "None").append("\n");
        report.append("- **Reason**: ").append(aiRecord.getReason() != null ? aiRecord.getReason() : "N/A").append("\n");
        
        report.append("\n");
    }
}
