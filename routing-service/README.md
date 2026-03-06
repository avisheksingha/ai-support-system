# Routing Service

The Routing Service is a microservice responsible for orchestrating the ticket routing workflow in the AI Support System. It coordinates with other services to determine the best agent or team for a given ticket.

## Features

- **Workflow Orchestration**: Manages the flow between Ticket Service, AI Analysis Service, and other downstream systems.
- **Intelligent Routing**: Uses AI-generated analysis (sentiment, urgency, intent) and rule-based logic to assign tickets.
- **Service Discovery**: Fully integrated with Eureka for dynamic service lookup.
- **Resilience**: Implements circuit breakers using Resilience4j to handle service downtime gracefully.

## Orchestration Flow

1. **Receive Request**: Triggered by a routing request for a specific ticket.
2. **Fetch Ticket Details**: Retrieves the latest ticket data from the **Ticket Service**.
3. **Request AI Analysis**: Calls the **AI Analysis Service** to get sentiment, urgency, and intent.
4. **Evaluate Routing Rules**: (Optional) Processes analysis results through a rule engine to determine the destination.
5. **Execute Routing**: Updates the **Ticket Service** with the routing action (e.g., assignment to agent/team, status update).

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
