# Workflow Runtime

Version: 1.0
Status: Current
Last Updated: 2026-07-11
Related ADRs:
- [ADR-001 Workflow Runtime](../adr/ADR-001-workflow-runtime.md)
Related Documents:
- [04-context-intelligence.md](04-context-intelligence.md)

The **Workflow Runtime** is the engine that drives execution. 

Instead of writing custom service methods for every AI use case, we define `WorkflowDefinition`s containing a sequence of `WorkflowStep`s.

## Workflow Lifecycle

1. **Trigger**: An event (e.g., Kafka `TicketCreatedEvent`) maps to a workflow definition.
2. **Context Creation**: A `WorkflowContext` is instantiated, storing variables and a unique `CorrelationId`.
3. **Step Execution**: The `WorkflowEngine` iterates through the defined steps.
4. **Completion**: If successful, the engine returns `SUCCESS`. If it encounters a known duplicate, it returns `SKIPPED`.

## Idempotency
The runtime natively supports idempotency by tracking `(correlationId, workflowDefinitionId, version)`. Duplicate Kafka messages are caught by the `WorkflowExecutionRepository` before any execution occurs.

## Key Interfaces
- `WorkflowEngine`: The orchestrator runner.
- `WorkflowDefinition`: The recipe (e.g., `AnalyzeWorkflowDefinition`).
- `WorkflowStep`: A discrete unit of work (e.g., `AssembleContextStep`, `AnalyzeTicketStep`).
