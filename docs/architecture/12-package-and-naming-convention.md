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

## Standard Top-Level Structure

Every service must follow this top-level structure:

```text
src/main/java
└── com.aisupport.<service>
    ├── config
    ├── application
    ├── domain
    └── infrastructure
```

Nothing else should exist at the top level unless absolutely required.

### 1. config

Contains framework configuration only (e.g., SecurityConfig, KafkaConfig, OpenApiConfig, JacksonConfig, BeanConfig, WebSocketConfig).

- No business logic.

### 2. application

Contains business use cases. This is where application orchestration happens.

- Organize by business capability/feature (e.g., ticket, workflow, governance, timeline, operations, customer, notification).
- Each feature contains: controller, service, dto, mapper, validator (if needed).
- *Exception*: Controllers may optionally remain in `infrastructure.web` to adhere strictly to Hexagonal Architecture, but standard naming still applies.

### 3. domain

Contains pure business concepts. Domain must NOT depend on Spring, Kafka, JPA, HTTP, Docker, or Infrastructure.

- Contains: model, event, repository (interfaces), exception, state, value objects, business rules.

### 4. infrastructure

Contains all technical implementations.

- Examples: persistence, messaging, client, mcp, notification, websocket, storage.
- Contains: JPA repositories, Kafka publishers/consumers, REST clients, DB/API implementations.

## Feature-first Organization

Within the Application layer, organize by feature instead of technical artifacts.

**GOOD:**

```text
application
├── ticket
├── customer
├── governance
└── workflow
```

**BAD:**

```text
application
├── controller
├── dto
├── mapper
└── service
```

## Standard Naming Convention

| Artifact | Convention | Examples | Notes |
| ---------- | ------------ | ---------- | ------- |
| Controllers | `<Feature>Controller` | `TicketController`, `WorkflowController` | Avoid vague names |
| Services | `<Feature>Service` | `TicketService`, `WorkflowService` | Avoid vague names |
| Repositories | `<Entity>Repository` | `TicketRepository`, `WorkflowRepository` | - |
| DTOs | Explicit Names | `CreateTicketRequest`, `TicketSummaryResponse` | Avoid generic DTO names |
| Mappers | `<Entity>Mapper` | `TicketMapper`, `WorkflowMapper` | - |
| Providers | `<Capability>Provider` | `ConversationContextProvider`, `GithubToolProvider` | - |
| Factories | `<Capability>Factory` | `WorkflowFactory`, `PromptFactory` | - |
| Managers | `<Resource>Manager` | `TokenBudgetManager`, `ConnectionManager` | Reserve for lifecycle/resource management. Avoid if Service or Engine fits better. |
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
- **Exceptions & Steps**: It is acceptable and encouraged to keep dedicated `exception` and `steps` packages inside feature folders to improve discoverability and prevent mixing business logic with error handling.

## Architecture Rules

- Business logic belongs in: `application`
- Business concepts belong in: `domain`
- Framework code belongs in: `infrastructure`
- Configuration belongs in: `config`
- Controllers should remain thin.
- Services coordinate use cases.
- Domain owns business rules.
- Infrastructure owns technical details.

## Migration Strategy

- Defined as the official project convention.
- Applied completely to `ai-orchestration-service` during its cleanup phase.
- Applies to all new services going forward.
- Gradually migrate older services only when significant work is already being performed on them.
