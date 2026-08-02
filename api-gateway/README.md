# API Gateway

The API Gateway is the central entry point for all external client requests to the AI Support System. Built with Spring Cloud Gateway **(WebFlux)**, it asynchronously routes incoming traffic to the appropriate backend microservices with high concurrent throughput.

## Features

- **Centralized Routing**: Routes `/api/v1/**` requests to `auth-service`, `ticket-service`, `ai-analysis-service`, `routing-service`, `rag-service`, and `ai-orchestration-service`.
- **JWT Authentication**: `JwtAuthenticationFilter` validates access tokens on every request and injects verified identity headers for downstream services. `CookieGuardFilter` provides defense-in-depth CSRF protection for stateless JWT flows.
- **WebSocket Proxy**: Forwards `/ws/**` to `ticket-service` for STOMP-over-SockJS real-time communication.
- **Swagger Aggregation**: Aggregates OpenAPI documentation from all backend services into a single Swagger UI at `/swagger-ui/index.html`.
- **Observability & Tracing**: A global `CorrelationIdFilter` generates and inserts an `X-Correlation-Id` for every incoming request, while a `LoggingFilter` traces all lifecycle events.
- **Service Discovery Integration**: Uses Eureka to dynamically discover and route to healthy service instances.
- **Load Balancing**: Distributes requests across multiple instances of a service via Spring Cloud LoadBalancer.

## Route Mapping

| Route Prefix               | Target Service              |
| -------------------------- | --------------------------- |
| `/api/v1/auth/**`          | `auth-service`              |
| `/api/v1/tickets/**`       | `ticket-service`            |
| `/api/v1/analysis/**`      | `ai-analysis-service`       |
| `/api/v1/routing/**`       | `routing-service`           |
| `/api/v1/rag/**`           | `rag-service`               |
| `/api/v1/orchestration/**` | `ai-orchestration-service`  |
| `/ws/**`                   | `ticket-service` (WebSocket)|

> [!NOTE]
> Internal `/api/internal/**` endpoints on domain services are **not** routed through the gateway. They are accessed directly via service discovery by the orchestrator.

## Configuration

| Property          | Value    | Description                                 |
| ----------------- | ------:  | ------------------------------------------- |
| Server Port       |    8080  | Port where gateway runs                     |
| Service Discovery | Enabled  | Registers with and polls Eureka             |
| JWT Secret        | Required | Shared with `auth-service` via `JWT_SECRET` |

## Running Locally

1. Ensure the `discovery-service` is running.
2. Set `JWT_SECRET` in your environment.
3. Start the API Gateway:

```bash
mvn spring-boot:run
```

OpenAPI UI: `http://localhost:8080/swagger-ui/index.html` (aggregated from all services)
