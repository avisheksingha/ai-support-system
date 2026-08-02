# Custom Copilot Agents for AI Support System

This directory contains service-specific agent guides aligned with the current codebase.

## Quick Agent Selector

| Service                  | Agent                                             | Port | Focus                                                              |
| ------------------------ | ------------------------------------------------- | ---- | ------------------------------------------------------------------ |
| `api-gateway`            | [API Gateway Agent](gateway-agent.md)             | 8080 | Gateway routing + correlation id propagation                       |
| `auth-service`           | [Auth Service Agent](auth-agent.md)               | 8081 | Authentication, JWT issuance, and user management                  |
| `discovery-service`      | [Discovery Service Agent](discovery-agent.md)     | 8761 | Eureka registry and service discovery                              |
| `ticket-service`         | [Ticket Service Agent](ticket-agent.md)           | 8082 | Ticket REST APIs, lifecycle, outbox, event consumers               |
| `ai-analysis-service`    | [AI Analysis Agent](analysis-agent.md)            | 8083 | AI analysis + internal REST API for orchestrator                   |
| `routing-service`        | [Routing Agent](router-agent.md)                  | 8084 | Rule evaluation + internal REST API for orchestrator               |
| `rag-service`            | [RAG Agent](rag-agent.md)                         | 8085 | RAG generation, vector store + internal REST API for orchestrator  |
| `ai-orchestration-service`| [Orchestration Agent](orchestration-agent.md)    | 8086 | Central AI workflow runtime, tool calling, and governance          |
| `ai-support-dashboard`   | [Dashboard Agent](dashboard-agent.md)             | 5173 | GenAI Operations Console frontend UI                               |

## How to Use

1. Pick the service you are changing.
2. Open the matching agent file first.
3. Use the listed key files, commands, and constraints from that agent.
4. Validate assumptions against source paths in the same module before coding.

## Current Architecture Notes

- External traffic enters via `api-gateway`.
- Service discovery is handled by `discovery-service` (Eureka).
- The `ai-orchestration-service` (8086) is the central AI workflow runtime. It consumes `ticket-created` events from Kafka and orchestrates the domain capability services via synchronous internal REST APIs.
- `ai-analysis-service`, `routing-service`, and `rag-service` expose `/api/internal/**` endpoints consumed by the orchestrator. They are **not** directly event-driven for ticket processing.
- Integration flow:
  - `ticket-service` publishes `ticket-created` to Kafka.
  - `ai-orchestration-service` consumes `ticket-created` and calls domain services via REST (Tool Calling).
  - `ai-orchestration-service` publishes `ticket-orchestrated` back to Kafka.
  - `ticket-service` consumes `ticket-orchestrated` and updates the ticket.

## Suggested Reading Order

1. [Auth Service Agent](auth-agent.md)
2. [Ticket Service Agent](ticket-agent.md)
3. [Orchestration Agent](orchestration-agent.md)
4. [AI Analysis Agent](analysis-agent.md)
5. [Routing Agent](router-agent.md)
6. [RAG Agent](rag-agent.md)
7. [API Gateway Agent](gateway-agent.md)
8. [Discovery Service Agent](discovery-agent.md)
9. [Dashboard Agent](dashboard-agent.md)

## Related Docs

- `README.md`
- `ARCHITECTURE.md`
- `TESTING.md`
- `.github/copilot-instructions.md`

---

**Last Updated:** 2026-08-02
**Status:** Aligned with orchestrator-based architecture (V1).
