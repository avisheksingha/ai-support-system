# Custom Copilot Agents for AI Support System

This directory contains service-specific agent guides aligned with the current codebase.

## Quick Agent Selector

| Service | Agent | Port | Focus |
|---------|-------|------|-------|
| `api-gateway` | [API Gateway Agent](gateway-agent.md) | 8080 | Gateway routing + correlation id propagation |
| `auth-service` | [Auth Service Agent](auth-agent.md) | 8081 | Authentication, JWT issuance, and user management |
| `discovery-service` | [Discovery Service Agent](discovery-agent.md) | 8761 | Eureka registry and service discovery |
| `ticket-service` | [Ticket Service Agent](ticket-agent.md) | 8082 | Ticket REST APIs, lifecycle, outbox, event consumers |
| `ai-analysis-service` | [AI Analysis Agent](analysis-agent.md) | 8083 | Kafka consume/analyze/publish + query APIs |
| `routing-service` | [Routing Agent](router-agent.md) | 8084 | Kafka consume, rule evaluation, outbox publish |
| `rag-service` | [RAG Agent](rag-agent.md) | 8085 | Kafka consume, RAG generation, vector store, outbox publish |

## How to Use

1. Pick the service you are changing.
2. Open the matching agent file first.
3. Use the listed key files, commands, and constraints from that agent.
4. Validate assumptions against source paths in the same module before coding.

## Current Architecture Notes

- External traffic enters via `api-gateway`.
- Service discovery is handled by `discovery-service` (Eureka).
- `auth-service`, `ticket-service`, and `ai-analysis-service` expose REST controllers.
- `routing-service` and `rag-service` are currently event-driven (no public REST controllers).
- Integration flow is outbox + Kafka topics:
  - `ticket-created`
  - `ticket-analyzed`
  - `ticket-routed`
  - `ticket-rag-response`

## Suggested Reading Order

1. [Auth Service Agent](auth-agent.md)
2. [Ticket Service Agent](ticket-agent.md)
3. [AI Analysis Agent](analysis-agent.md)
4. [Routing Agent](router-agent.md)
5. [RAG Agent](rag-agent.md)
6. [API Gateway Agent](gateway-agent.md)
7. [Discovery Service Agent](discovery-agent.md)

## Related Docs

- `README.md`
- `ARCHITECTURE.md`
- `TESTING.md`
- `.github/copilot-instructions.md`

---

**Last Updated:** 2026-07-05
**Status:** Aligned with current module structure and symmetric 10-header schema.
