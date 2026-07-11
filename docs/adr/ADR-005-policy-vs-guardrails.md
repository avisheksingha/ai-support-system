# ADR 005: Policy vs Guardrails

## Status
Accepted

## Problem
AI execution must be governed, but merging business rules ("Are we allowed to do this?") with safety checks ("Is this content toxic or leaky?") into a single "Validation" layer creates bloated components that are difficult to update or audit.

## Decision
We formally separated Governance into two distinct layers:
1. **Policies (`PolicyEngine`)**: Execute *before* the Agent loop starts. Evaluates business rules (e.g., "Is this a VIP ticket?"). Outputs a `PolicyDecision` (`ALLOW`, `DENY`, `REQUIRE_APPROVAL`, `REROUTE`).
2. **Guardrails (`GuardrailPipeline`)**: Execute *during* the Agent loop, wrapping both input prompts and output responses. Evaluates safety (e.g., PII redaction, toxicity). Outputs a `GuardrailResult` (`ALLOW`, `MODIFY`, `BLOCK`, `WARN`).

## Consequences
- **Positive**: Strict separation of concerns allows compliance teams to manage Guardrails while product teams manage Policies.
- **Positive**: Guardrails can natively `MODIFY` content (e.g., mask credit cards) without stopping execution.
- **Negative**: Adds multiple interception layers, increasing cognitive load for developers debugging an execution trace.
