# Ticket Service

Microservice responsible for managing support tickets throughout their lifecycle.

## Features

- **CRUD Operations**: Create, read, update, and delete tickets.
- **State Machine**: Enforces strict status transition logic (e.g., `NEW` -> `ANALYZING` -> `ANALYZED` -> `ASSIGNED`).
- **Manual & Automated Assignment**: Assign tickets to agents manually or via the Routing Service.
- **Observability**: Implements Distributed Tracing via `CorrelationIdFilter` to propagate `X-Correlation-Id` directly into MDC for logs.
- **Event-Driven Architecture**: Uses **Apache Kafka** for asynchronous communication and status updates. Publishes events using a robust **Outbox Pattern** with retry semantics.
- **AI Integration**: Integration with **AI Analysis Service** for automated tagging (sentiment, intent, urgency) and **RAG Service** for context-aware suggestions.
- **Service Discovery**: Registers with Eureka for dynamic invocation.
- **Resilience**: Resilient communication using Circuit Breakers (Resilience4j).

## API Endpoints

- `POST /api/v1/tickets`: Create a new ticket
- `GET /api/v1/tickets/{ticketNumber}`: Get ticket details
- `GET /api/v1/tickets`: List all tickets (optional status filter)
- `PATCH /api/v1/tickets/{ticketNumber}/status`: Update status
- `PATCH /api/v1/tickets/{ticketNumber}/assign`: Assign to agent

## Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8082 | Port where service runs |
| Database | PostgreSQL | `ticket_db` |
| Service Discovery | Enabled | Registers with Eureka |

## Running Locally

```bash
mvn spring-boot:run
```
