package com.aisupport.orchestration.application.operations.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationsDashboardResponse {
    private OperationsOverviewDTO overview;
    private List<WorkflowSummaryDTO> recentExecutions;
}
