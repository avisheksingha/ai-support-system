# Routing Service

The Routing Service is a microservice responsible for orchestrating the ticket routing workflow in the AI Support System. It coordinates with other services to determine the best agent or team for a given ticket.

## Features

- **Workflow Orchestration**: Manages the flow between Ticket Service, AI Analysis Service, and Rule Engine Service.
- **Intelligent Routing**: Uses AI-generated analysis and rule-based logic to assign tickets.
- **Service Discovery**: Fully integrated with Eureka for dynamic service lookup.
- **Resilience**: Implements circuit breakers using Resilience4j to handle service downtime gracefully.

## Orchestration Flow

1. Receives a routing request for a ticket.
2. Fetches ticket details from **Ticket Service**.
3. Requests AI analysis from **AI Analysis Service**.
4. Sends ticket details and AI analysis to **Rule Engine Service** for evaluation.
5. Updates the **Ticket Service** with the identified routing action (e.g., assignment, status change).

## API Endpoints

- `POST /api/v1/routing/route`: Route a ticket through the complete workflow using the provided request body.
- `POST /api/v1/routing/route/{ticketId}`: Trigger the routing workflow for a specific ticket ID.

## Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8085 | Port where service runs |
| Service Discovery | Enabled | Registers with Eureka |

## Running Locally

```bash
mvn spring-boot:run
```
