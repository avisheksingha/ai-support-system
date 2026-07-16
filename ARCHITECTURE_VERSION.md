# AI Support System Backend Architecture

**Version 1.0**

## Status
Structurally Frozen for V1

## Breaking Changes
None planned before V2

## Major Milestones
- **Phase 1**: Architecture Foundation (Microservice Scaffolding)
- **Phase 2**: Workflow Runtime (Idempotency, Event Handling)
- **Phase 3**: Context Intelligence & AI Reasoning
- **Phase 4**: AI Platform Governance
- **Phase 5**: Operations Observability
- **Phase 6**: Package & Naming Convention Standardization (Complete)

## V1 Backend Architecture Freeze

As of Phase 6 completion, the entire microservice backend is officially **V1 Structurally Frozen**.

### Final Package Philosophy
The backend strictly adheres to a Dual Package Philosophy:
1. **The Flat Architecture (Standard Microservices)**: `ticket-service`, `auth-service`, `ai-analysis-service`, `routing-service`, `rag-service`.
   - Permitted packages: `config`, `controller`, `service`, `repository`, `entity`, `dto/request`, `dto/response`, `mapper`, `consumer`, `producer`, `client`, `outbox`, `exception`, `filter`, `util`, `constants`.
   - Strictly flat layout without abstract layer domains (`application`, `domain`, `infrastructure`).
2. **The Layered Architecture (Orchestrator ONLY)**: `ai-orchestration-service`.
   - Feature-first Hexagonal Architecture: `config`, `application`, `domain`, `infrastructure`.

### Shared Architectural Principles
- **Outbox Consistency**: Shared conceptual infrastructure (e.g., the `outbox` package) remains structurally identical across all services.
- **DTO Isolation**: Absolute separation between `dto/request` and `dto/response`.
- **Feature Flattening**: Business logic (controllers, services) remains at the root level, not nested deeply inside internal feature directories.

### Services Standardized
- `ticket-service`
- `auth-service`
- `ai-analysis-service`
- `routing-service`
- `rag-service`
- `common-library`
- `ai-orchestration-service` (Using the Layered standard)

### Services Intentionally Excluded
- `api-gateway`: Naturally matches the flat philosophy (`config`, `filter`).
- `discovery-service`: Contains only the root application class.

## V1 Freeze Rules
We will avoid further architectural refactoring. Future backend work should be strictly limited to:
- Bug fixes
- Security improvements
- Performance optimizations
- Missing integrations
- Tests
- Documentation

New capabilities (Multi-Agent, Real MCP Protocol, Semantic Cache, Model Routing, etc.) belong to the V2 roadmap.

## Frozen Contracts
The following architectural abstractions are considered stable and frozen. They should not be redesigned unless a demonstrated implementation limitation requires it:
- `WorkflowEngine`, `WorkflowDefinition`, `WorkflowStep`, `WorkflowContext`
- `ContextProvider`
- `Agent`, `AgentSession`, `AgentRequest`, `AgentResponse`
- `ToolRegistry`, `ToolExecutor`
- `PolicyEngine`, `AiPolicy`
- `GuardrailPipeline`, `InputGuardrail`, `OutputGuardrail`
