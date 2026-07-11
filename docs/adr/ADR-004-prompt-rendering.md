# ADR 004: Prompt Rendering

## Status
Accepted

## Problem
Injecting dynamic context (like retrieved RAG articles or conversation histories) into a prompt string inside Java classes leads to massive, hard-to-read code that mixes logic with natural language instructions.

## Decision
We use a **Prompt Renderer** coupled with `.st` (StringTemplate) markdown files. The Java code merely builds the `AgentRequest` and passes variables to the renderer, which merges the variables into the `.st` file using placeholder syntax `{variable}`.

## Consequences
- **Positive**: Prompt engineering happens in markdown files, making it easy for non-developers to review instructions.
- **Positive**: Java classes remain small and strictly focused on logic.
- **Negative**: Syntax collisions with JSON format instructions (which also use curly braces) require careful escaping.
