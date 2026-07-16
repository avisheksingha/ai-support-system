# 12. Package and Naming Convention

As the project grows into multiple microservices, a consistent package structure and naming convention is required across the entire codebase. This is an architectural standardization rule.

## Objective

Define a common package convention for every microservice.
The goals are:

- Consistent navigation
- Predictable package locations
- Better discoverability
- Reduced cognitive load
- Easier onboarding
- Long-term maintainability

This convention applies to:

- ticket-service
- ai-analysis-service
- routing-service
- rag-service
- ai-orchestration-service
- future services

## Guiding Principles

- Organize by responsibility before implementation details.
- Avoid unnecessary package depth.
- Avoid package fragmentation.
- Avoid generic naming.
- Every class should be discoverable without opening dozens of folders.

## Dual Package Philosophy

The project utilizes a dual package philosophy depending on the complexity and domain of the microservice.

### 1. The Flat Architecture (Standard Microservices)
For standard domain microservices (e.g., `ticket-service`, `auth-service`, `ai-analysis-service`, `routing-service`, `rag-service`), we strictly follow a flat, simple, and immediately understandable Spring Boot architecture.

**We do NOT introduce `application`, `domain`, or `infrastructure` packages in these services.**

The permitted top-level packages are:
- `config`: Framework configurations.
- `controller`: REST APIs.
- `service`: Business logic.
- `repository`: Database access.
- `entity`: JPA models.
- `dto`: Split strictly into `dto/request` and `dto/response`.
- `mapper`: Object mapping logic.
- `consumer` / `producer`: Kafka messaging.
- `client`: External REST/Feign clients.
- `outbox`: Shared outbox pattern infrastructure.
- `exception`: Dedicated exceptions.
- `filter`: Security or web filters.
- `llm` / `vector` / `embedding`: Domain-specific integrations (if needed).
- `util` / `constants`: Shared utilities.

*Only create packages that actually make sense for the service (e.g., no `repository` if no database exists).*

### 2. The Layered Architecture (AI Orchestration Service ONLY)
Due to its unique role in coordinating complex multi-agent workflows, the `ai-orchestration-service` uses a strict feature-first Hexagonal Architecture structure:

```text
src/main/java
└── com.aisupport.orchestration
    ├── config
    ├── application
    ├── domain
    └── infrastructure
```
- `config`: Framework configurations only.
- `application`: Business orchestration, grouped by feature.
- `domain`: Pure business concepts decoupled from technical frameworks.
- `infrastructure`: Technical implementations.

---

## Standard Naming Convention

| Artifact | Convention | Examples | Notes |
| ---------- | ------------ | ---------- | ------- |
| Controllers | `<Feature>Controller` | `CustomerTicketController`, `WorkflowController` | Avoid vague names |
| Services | `<Feature>Service` | `TicketService`, `WorkflowService` | Avoid vague names |
| Repositories | `<Entity>Repository` | `TicketRepository`, `WorkflowRepository` | - |
| DTOs | Explicit Names | `CreateTicketRequest`, `TicketSummaryResponse` | Avoid generic DTO names |
| Mappers | `<Entity>Mapper` | `TicketMapper`, `WorkflowMapper` | - |
| Providers | `<Capability>Provider` | `ConversationContextProvider`, `GithubToolProvider` | - |
| Factories | `<Capability>Factory` | `WorkflowFactory`, `PromptFactory` | - |
| Managers | `<Resource>Manager` | `TokenBudgetManager`, `ConnectionManager` | Reserve for lifecycle/resource management. |
| Engines | `<Execution>Engine` | `WorkflowEngine`, `PolicyEngine` | Use only for execution engines. |
| Executors | `<Action>Executor` | `ToolExecutor`, `WorkflowExecutor` | Use only for executing actions. Avoid generic `Executor`. |
| Exceptions | `<Feature>Exception` | `WorkflowNotFoundException` | Avoid overly generic exception names. |
| Interfaces | Simple Names | `WorkflowEngine`, `ToolExecutor` | Avoid prefixes or suffixes unless multiple implementations exist. |
| Implementations | `<Name>Impl` | `WorkflowEngineImpl`, `ToolExecutorImpl` | Use `Impl` only when multiple implementations genuinely exist. |

## General Naming Rules

- Every class should explain its responsibility.
- Avoid generic terms like `Helper`, `Processor`, `Handler`, `Manager`, `Util`, `Context`, `ServiceImpl` unless they truly represent those responsibilities. Prefer explicit names.

## Package Rules

- Avoid packages containing only one class unless justified.
- Avoid `impl`, `internal`, `common`, `misc`, `helper` unless there is a clear architectural reason.
- Flatten package hierarchies where possible.
- **Exceptions**: It is acceptable and encouraged to keep dedicated `exception` packages to improve discoverability.
- **Outbox**: Keep the `outbox` package structurally identical across all services that use it.

## Architecture Freeze (V1)

**The backend architecture is structurally frozen for V1.** 
All microservices have been fully standardized to these conventions. Avoid further architectural refactoring or introducing new package hierarchies unless there is a clear architectural justification. New features should follow the established package conventions exactly.
