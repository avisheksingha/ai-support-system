# Ticket Service

Microservice responsible for managing support tickets throughout their lifecycle.

## Features

- **Role-aware Ticket Operations**: Customers create and view their own tickets; agents and administrators manage the support queue.
- **State Machine**: Enforces strict status transition logic (e.g., `NEW` -> `ANALYZING` -> `ANALYZED` -> `ASSIGNED`).
- **Manual & Automated Assignment**: Assign tickets to agents manually or via the Routing Service.
- **Observability**: Implements Distributed Tracing via `CorrelationIdFilter` to propagate `X-Correlation-Id` directly into MDC for logs.
- **Event-Driven Architecture**: Uses **Apache Kafka** for asynchronous communication and status updates. Publishes events using a robust **Outbox Pattern** with retry semantics.
- **AI Integration**: Triggers asynchronous AI workflows (via the **AI Orchestration Service**) for automated tagging (sentiment, intent, urgency), intelligent routing, and context-aware suggestions.
- **Service Discovery**: Registers with Eureka for dynamic invocation.
- **Resilience**: Resilient communication using Circuit Breakers (Resilience4j).

## API Endpoints

All endpoints are routed through the API Gateway. A valid JWT is required; authorization is enforced by role.

### Customer (`CUSTOMER`)

- `POST /api/v1/tickets`: Create a ticket (`subject`, `message`, optional `bypassSoftValidation`)
- `GET /api/v1/tickets/my`: List the authenticated customer's tickets
- `GET /api/v1/tickets/my/{ticketNumber}`: Get an owned ticket
- `GET /api/v1/tickets/my/{ticketNumber}/messages`: Get messages for an owned ticket
- `POST /api/v1/tickets/my/{ticketNumber}/messages`: Add a customer message

### Support management (`AGENT` or `ADMIN`)

- `GET /api/v1/tickets`: List tickets (optional `status`)
- `GET /api/v1/tickets/{ticketNumber}` and `GET /api/v1/tickets/id/{id}`: Retrieve a ticket
- `PATCH /api/v1/tickets/{ticketNumber}/status?status=...`: Update status
- `PATCH /api/v1/tickets/{ticketNumber}/assign?assignedTo=...`: Assign an agent
- `PATCH /api/v1/tickets/{ticketNumber}/priority?priority=...`: Update priority
- `GET` and `POST /api/v1/tickets/{ticketNumber}/messages`: Manage ticket messages
- `GET /api/v1/tickets/summary/agent`: Retrieve an agent/team summary

Ticket deletion is not implemented.

## Configuration

| Property | Value | Description |
| ---------- | ------- | ------------- |
| Server Port | 8082 | Port where service runs |
| Database | PostgreSQL | `ticket_db` |
| Service Discovery | Enabled | Registers with Eureka |

## Running Locally

```bash
mvn spring-boot:run
```
