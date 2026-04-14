# Custom Copilot Agents for AI Support System

This directory contains service-specific agent guides aligned with the current codebase.

## Quick Agent Selector

| Service | Agent | Port | Focus |
|---------|-------|------|-------|
| `api-gateway` | [API Gateway Agent](api-gateway-agent.md) | 8080 | Gateway routing + correlation id propagation |
| `discovery-service` | [Discovery Service Agent](discovery-service-agent.md) | 8761 | Eureka registry and service discovery |
| `ticket-service` | [Ticket Service Agent](ticket-service-agent.md) | 8082 | Ticket REST APIs, lifecycle, outbox, event consumers |
| `ai-analysis-service` | [AI Analysis Agent](ai-analysis-agent.md) | 8083 | Kafka consume/analyze/publish + query APIs |
| `routing-service` | [Routing Agent](routing-agent.md) | 8084 | Kafka consume, rule evaluation, outbox publish |
| `rag-service` | [RAG Agent](rag-agent.md) | 8085 | Kafka consume, RAG generation, vector store, outbox publish |

## How to Use

1. Pick the service you are changing.
2. Open the matching agent file first.
3. Use the listed key files, commands, and constraints from that agent.
4. Validate assumptions against source paths in the same module before coding.

## Current Architecture Notes

- External traffic enters via `api-gateway`.
- Service discovery is handled by `discovery-service` (Eureka).
- `ticket-service` and `ai-analysis-service` expose REST controllers.
- `routing-service` and `rag-service` are currently event-driven (no public REST controllers).
- Integration flow is outbox + Kafka topics:
  - `ticket-created`
  - `ticket-analyzed`
  - `ticket-routed`
  - `ticket-rag-response`

## Suggested Reading Order

1. [Ticket Service Agent](ticket-service-agent.md)
2. [AI Analysis Agent](ai-analysis-agent.md)
3. [Routing Agent](routing-agent.md)
4. [RAG Agent](rag-agent.md)
5. [API Gateway Agent](api-gateway-agent.md)
6. [Discovery Service Agent](discovery-service-agent.md)

## Related Docs

- `README.md`
- `ARCHITECTURE.md`
- `TESTING.md`
- `.github/copilot-instructions.md`

---

**Last Updated:** 2026-04-12
**Status:** Aligned with current module structure and runtime behavior.
