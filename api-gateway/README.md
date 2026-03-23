# API Gateway

The API Gateway is the central entry point for all external client requests to the AI Support System. Built with Spring Cloud Gateway **(WebFlux)**, it asynchronously routes incoming traffic to the appropriate backend microservices with high concurrent throughput.

## Features

- **Centralized Routing**: Directs API calls to `ticket-service`, `ai-analysis-service`, `routing-service`, `rag-service`, etc.
- **Observability & Tracing**: A global `CorrelationIdFilter` generates and inserts an `X-Correlation-Id` for every incoming request, while a `LoggingFilter` traces all lifecycle events.
- **Service Discovery Integration**: Uses Eureka to dynamically discover and route to healthy service instances.
- **Load Balancing**: Distributes requests across multiple instances of a service.
- **Security & Filtering**: Can be extended to handle authentication, rate limiting, and request transformation.

## Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8080 | Port where gateway runs |
| Service Discovery | Enabled | Registers with and polls Eureka |

## Running Locally

1. Ensure the `discovery-service` is running.
2. Start the API Gateway:

```bash
mvn spring-boot:run
```
