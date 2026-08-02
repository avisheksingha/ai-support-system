# Orchestration Service Agent

**Role:** Core AI Runtime & Workflow Orchestrator

**Port:** 8086

**Responsibility:** The central intelligence layer. It consumes `ticket-created`, executes dynamic workflows (like `AnalyzeWorkflowDefinition`), orchestrates domain capabilities via synchronous internal REST Tool Calling (Analysis, Routing, RAG), enforces AI governance/policies, and publishes the final `ticket-orchestrated` event. It also exposes public REST APIs for dashboards, knowledge base management, and operations via the API Gateway.

## Quick Commands

### Build
```bash
mvn -pl ai-orchestration-service clean install
```

### Run Service
```bash
cd ai-orchestration-service && mvn spring-boot:run
```

### Run Tests
```bash
mvn -pl ai-orchestration-service test
```

### Swagger
```text
http://localhost:8086/swagger-ui.html
```

## Key Files

- **Application Controllers:** `src/main/java/com/aisupport/orchestration/application/controller/` (e.g., `TicketContextController.java`, `DashboardController.java`, `KnowledgeBaseController.java`)
- **Workflow Definitions:** `src/main/java/com/aisupport/orchestration/application/workflow/` (e.g., `AnalyzeWorkflowDefinition.java`)
- **Agent/LLM Layer:** `src/main/java/com/aisupport/orchestration/domain/agent/`
- **Tool Execution:** `src/main/java/com/aisupport/orchestration/domain/tool/`
- **Governance & Guardrails:** `src/main/java/com/aisupport/orchestration/domain/governance/`
- **Kafka Consumers:** `src/main/java/com/aisupport/orchestration/infrastructure/messaging/consumer/OrchestrationEventConsumer.java`
- **Entities:** Hexagonal architecture style (`domain/entity` vs `infrastructure/persistence/entity`).
- **Repositories:** `src/main/java/com/aisupport/orchestration/infrastructure/persistence/repository/`

## Key Responsibilities & Flow (Analyze Workflow)

1. `OrchestrationEventConsumer` receives `ticket-created`.
2. Triggers `AnalyzeWorkflowDefinition`.
3. Evaluates governance policies and guardrails.
4. Executes the AI model which seamlessly invokes internal domain service tools (Analysis Tool, RAG Tool, Routing Tool) over REST.
5. Emits `TicketOrchestratedEvent` through the outbox.

## Current API Endpoints

(Routed via API Gateway `/api/v1/orchestration/**`)

- **Ticket Context:** `/tickets/{ticketId}/timeline`, `/tickets/{ticketId}/workspace`
- **Dashboards:** `/dashboard/admin`, `/dashboard/agent`, `/dashboard/customer`
- **Knowledge Base:** `/knowledge-base/search`, `/knowledge-base/articles`
- **Governance:** `/governance/overview`, `/governance/audit-logs`
- **Operations:** `/operations/overview`

## Database Snapshot

### orchestration_workflows
- `id` (UUID, PK)
- `correlation_id`
- `ticket_id`
- `workflow_type`, `status`
- `started_at`, `completed_at`

### agent_sessions
- `id` (UUID, PK)
- `workflow_id`
- `model`, `prompt_tokens`, `completion_tokens`

### audit_logs
- `id` (UUID, PK)
- `correlation_id`, `event_type`, `action`, `status`
- `details` (JSONB)

## Important Rules

- Follow the Hexagonal Architecture (Ports and Adapters). Do not leak `infrastructure` details into `domain`.
- Use the `common-library` for DTOs when communicating across services, but map them to domain entities internally.
- All outbound domain capability invocations (Analysis, Routing, RAG) must be implemented as `ToolExecutor` implementations.

## Environment Variables

- `SPRING_PROFILES_ACTIVE`
- `DB_USERNAME`, `DB_PASSWORD`
- `GCP_PROJECT_ID`, `GCP_LOCATION`, `GOOGLE_APPLICATION_CREDENTIALS`

## Related Services

- Consumes from `ticket-service` via `ticket-created`.
- Synchronously invokes `ai-analysis-service`, `rag-service`, and `routing-service` via REST tool calls.
- Produces `ticket-orchestrated` for `ticket-service` to consume.
