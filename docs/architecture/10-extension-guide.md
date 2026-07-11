# Extension Guide

Version: 1.0
Status: Current
Last Updated: 2026-07-11

The AI Orchestration Platform is designed to be easily extensible without modifying core runtime classes.

## Add a Workflow
1. Create a class implementing `WorkflowDefinition`.
2. Register the workflow against an Event Trigger (e.g., `TicketCreatedEvent`).
3. Define the steps inside the definition (e.g., `AssembleContextStep`, `AnalyzeTicketStep`).

## Add a Tool
1. Create a class implementing `ToolDefinition`.
2. Register the tool in the `ToolRegistry`.
3. Include the tool name in the `allowedCapabilities` list when passing the `AgentRequest`.

## Add a Context Provider
1. Create a class implementing `ContextProvider`.
2. Annotate with `@Order` to specify priority.
3. The context will automatically be discovered and assembled by the `AssembleContextStep`.

## Add a Guardrail
1. Create a class implementing `InputGuardrail` or `OutputGuardrail`.
2. Register it as a Spring Bean.
3. The `GuardrailPipeline` will automatically execute it during the Agent Session.
