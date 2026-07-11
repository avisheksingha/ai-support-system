# ADR 002: Context Providers

## Status
Accepted

## Problem
Before an LLM can perform reasoning, it requires significant context (e.g., ticket history, customer profiles, knowledge base articles). Fetching this data inside the Agent layer tightly couples the Agent to external data sources.

## Decision
We abstracted context retrieval into a **Context Intelligence** pipeline. The `AssembleContextStep` delegates to multiple typed `ContextProvider` implementations. Each provider is responsible for fetching its own domain's data (e.g., `KnowledgeContextProvider`) and adding it to the `WorkflowContext`.

## Consequences
- **Positive**: Agent layer has zero awareness of REST clients or databases.
- **Positive**: We can concurrently assemble context.
- **Negative**: The `WorkflowContext` becomes a generic property bag, which requires strict typing or keys to avoid collisions.
