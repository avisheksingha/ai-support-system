package com.aisupport.orchestration.infrastructure.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
@RequiredArgsConstructor
public class RuntimeCoverageReporter implements WorkflowExecutionListener {

    private static final String REPORT_PATH = "target/runtime-coverage.md";
    
    private final WorkflowExecutionRepository workflowRepository;
    private final AiExecutionRecordRepository aiRecordRepository;

    @Override
    public void beforeWorkflow(WorkflowContext context) { }

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
        report.append("- **Ticket ID**: ").append(context.getTicketId()).append("\n");
        report.append("- **Final Outcome**: ").append(finalStatus).append("\n\n");

        WorkflowExecutionEntity workflowExecution = workflowRepository.findById(context.getExecutionId()).orElse(null);
        if (workflowExecution != null) {
            report.append("## Status Details\n");
            report.append("- **Workflow Status**: ").append(workflowExecution.getState()).append("\n");
            report.append("- **Workflow Version**: v1.0\n"); // Assuming v1.0 for V1 portfolio
            report.append("- **Recoveries**: ").append(workflowExecution.getRecoveryCount()).append("\n");
            report.append("- **Checkpoint Used**: ").append(workflowExecution.getCurrentStep() != null ? workflowExecution.getCurrentStep() : "N/A").append("\n");
            
            if (workflowExecution.getCreatedAt() != null && workflowExecution.getUpdatedAt() != null) {
                long duration = java.time.Duration.between(workflowExecution.getCreatedAt(), workflowExecution.getUpdatedAt()).toMillis();
                report.append("- **Execution Time**: ").append(duration).append(" ms\n");
            }
            report.append("\n");
        }

        List<AiExecutionRecordEntity> aiRecords = aiRecordRepository.findByCorrelationId(context.getCorrelationId());
        if (aiRecords != null && !aiRecords.isEmpty()) {
            report.append("## AI Governance & Execution\n");
            for (AiExecutionRecordEntity record : aiRecords) {
                report.append("### AI Record (").append(record.getOutcome()).append(")\n");
                report.append("- **Prompt Hash**: ").append(record.getPromptHash()).append("\n");
                report.append("- **Model Profile**: ").append(record.getModelId()).append("\n");
                report.append("- **Tools Used**: ").append(record.getToolsInvoked() != null ? record.getToolsInvoked() : "None").append("\n");
                report.append("- **Policy Triggered**: ").append(record.getPolicyId() != null ? record.getPolicyId() + " (v" + record.getPolicyVersion() + ")" : "None").append("\n");
                report.append("- **Guardrail Triggered**: ").append(record.getGuardrailId() != null ? record.getGuardrailId() + " (v" + record.getGuardrailVersion() + ")" : "None").append("\n");
                report.append("- **Reason**: ").append(record.getReason() != null ? record.getReason() : "N/A").append("\n");
                report.append("- **Retries**: ").append("0").append("\n"); // V1 doesn't have agent retries
                report.append("\n");
            }
        }

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
}
