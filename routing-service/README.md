# Routing Service

The Routing Service is a Domain Capability Service responsible for deterministic ticket routing decisions in the AI Support System. It evaluates AI-generated analysis and business rules to determine the best agent or team for a given ticket.

## Features

- **Intelligent Routing**: Uses AI-generated analysis (sentiment, urgency, intent) and rule-based logic to assign tickets.
- **Service Discovery**: Fully integrated with Eureka for dynamic service lookup.
- **Resilience**: Implements circuit breakers using Resilience4j to handle service downtime gracefully.
- **Observability**: Implements Distributed Tracing via `CorrelationIdFilter` to extract and propagate `X-Correlation-Id` within MDC logs.

## Interactions

1. **Synchronous Invocation**: Provides routing capabilities to the `ai-orchestration-service` via REST (often consumed as Tools). The orchestrator passes the necessary context (such as sentiment, urgency, intent).
2. **Evaluate Routing Rules**: Processes the provided context through rule logic to determine the appropriate destination (e.g., Tier-1, Billing Dept).
3. **Asynchronous (Optional)**: Can optionally consume/produce events (like `TicketAnalyzedEvent`) for fallback async flows via a resilient Outbox Pattern.

## API Endpoints

### Internal REST (Orchestrator Tool Calling)

The routing service exposes internal endpoints consumed by `ai-orchestration-service` via tool calling. These are not routed through the API Gateway.

- `POST /api/internal/routing/route`: Evaluate routing rules for a ticket given analysis context.
- `GET /api/internal/routing/ticket/{ticketId}`: Retrieve existing routing decision for a ticket.

### Kafka Consumer

The service also consumes `ticket-analyzed` events from Kafka as a fallback async flow, processing them through the same routing logic.

## Configuration

| Property          | Value      | Description                 |
| ----------------- | ---------- | --------------------------- |
| Server Port       | 8084       | Port where service runs     |
| Service Discovery | Enabled    | Registers with Eureka       |

## Running Locally

```bash
mvn spring-boot:run
```

