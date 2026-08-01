# ADR 007: Preserve Event-Driven Choreography

## Status
Accepted

## Problem
With the introduction of the centralized AI Orchestration Service, we shifted from a pure event-driven choreography architecture (where domain services consumed outbox events independently) to an orchestrated V1 architecture (where the Orchestrator fetches data synchronously via internal REST endpoints). We needed a decision on whether to delete the legacy choreography code, event consumers, publishers, and contracts that are currently inactive in the primary flow.

## Decision
We will **preserve the original event-driven choreography implementation** as an alternative, secondary execution model. The new internal packages and synchronous REST communication between domain services and the AI Orchestration Service remain the default V1 implementation.

Specifically:
- Do NOT delete the original choreography classes.
- Do NOT remove Kafka publishers/consumers unless they are genuinely obsolete.
- Do NOT delete event contracts that are still valuable.

## Consequences
- **Positive**: The architecture remains highly flexible, allowing the execution strategy to evolve without requiring major refactoring.
- **Positive**: The original Kafka choreography flow can be re-enabled for future use, experimentation, or rollback scenarios.
- **Positive**: We acknowledge the system's growth from pure event choreography into a dual-mode platform supporting both decentralized business workflows and centralized AI orchestration.
- **Negative**: There will be dormant code in the codebase (disabled consumers or unused publishers) that must be maintained and understood by new developers as a secondary execution model, not legacy technical debt.
