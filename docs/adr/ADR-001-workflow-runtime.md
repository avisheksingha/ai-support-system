# ADR 001: Workflow Runtime

## Status
Accepted

## Problem
AI implementations often begin as simple REST endpoints calling LLMs. As complexity grows (tool use, RAG, multi-step reasoning), these endpoints become monolithic state machines that are hard to test and maintain. We needed a way to orchestrate multi-step AI execution while preserving clean separation of concerns.

## Decision
We implemented a custom lightweight **Workflow Runtime** inside the Orchestration service. It executes a sequence of `WorkflowStep`s grouped into a `WorkflowDefinition`.

## Consequences
- **Positive**: Complete separation between domain logic, context gathering, and LLM reasoning.
- **Positive**: Easy to add new capabilities by inserting a step into the workflow rather than refactoring a massive service class.
- **Negative**: Adds a layer of indirection compared to directly calling a Spring AI service. Requires developers to understand the Step paradigm.
