# Ticket Service

Microservice responsible for managing support tickets throughout their lifecycle.

## Features

- Create, read, update, and delete tickets
- Assign tickets to agents manually or via automated routing
- Track ticket status and priority
- Integration with **AI Analysis Service** for automated tagging
- Integration with **Routing Service** for intelligent ticket assignment
- Service Discovery via Eureka
- Resilient communication using Circuit Breaker (Resilience4j)

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
