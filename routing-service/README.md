# Routing Service

The Routing Service is a microservice responsible for orchestrating the ticket routing workflow in the AI Support System. It coordinates with other services to determine the best agent or team for a given ticket.

## Features

- **Workflow Orchestration**: Manages the flow between Ticket Service, AI Analysis Service, and other downstream systems.
- **Intelligent Routing**: Uses AI-generated analysis (sentiment, urgency, intent) and rule-based logic to assign tickets.
- **Service Discovery**: Fully integrated with Eureka for dynamic service lookup.
- **Resilience**: Implements circuit breakers using Resilience4j to handle service downtime gracefully.

## Orchestration Flow

1. **Receive Event**: Consumes a `TicketAnalyzedEvent` from Kafka.
2. **Evaluate Routing Rules**: Processes the AI-generated analysis results (Sentiment, Urgency, Intent) through rule logic to determine the appropriate destination (e.g., Tier-1, Billing Dept).
3. **Execute Routing**: Dispatches a call to the **Ticket Service** to assign the ticket to the determined agent or queue.

## API Endpoints

The Routing Service currently operates entirely asynchronously via Kafka events. It does not expose standard public REST endpoints for the client.

## Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8084 | Port where service runs |
| Service Discovery | Enabled | Registers with Eureka |

## Running Locally

```bash
mvn spring-boot:run
```
