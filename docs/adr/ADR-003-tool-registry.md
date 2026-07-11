# ADR 003: Tool Registry

## Status
Accepted

## Problem
Spring AI allows directly attaching `@Bean` functions to a `ChatClient` request. However, hardcoding tools into specific LLM requests makes it impossible to dynamically toggle tools based on policies, or share tools across different workflows.

## Decision
We implemented a **Tool Registry**. Tools are independently registered as `ToolDefinition`s. Workflows specify a list of `allowedCapabilities` when building an `AgentRequest`. The Agent resolves these strings against the registry at runtime.

## Consequences
- **Positive**: Workflow definitions strictly control what an LLM is allowed to execute.
- **Positive**: Tool implementations are completely decoupled from prompts or Agent loop logic.
- **Negative**: Re-implementing dynamic tool resolution on top of Spring AI requires careful management of Spring's internal `FunctionCallback` mechanisms.
