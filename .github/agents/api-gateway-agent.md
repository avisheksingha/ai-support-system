# API Gateway Agent

**Role:** Request Router & Entry Point

**Port:** 8080

**Responsibility:** Routes external HTTP traffic to internal services via Spring Cloud Gateway, propagates correlation ID, and centralizes gateway-level logging/routing behavior.

## Quick Commands

### Build
```bash
mvn -pl api-gateway clean install
```

### Run Service
```bash
cd api-gateway && mvn spring-boot:run
```

### Run Tests
```bash
mvn -pl api-gateway test
```

## Key Files

- **Main App:** `src/main/java/com/aisupport/gateway/ApiGatewayApplication.java`
- **Correlation Filter:** `src/main/java/com/aisupport/gateway/filter/CorrelationIdFilter.java`
- **Logging Filter:** `src/main/java/com/aisupport/gateway/filter/LoggingFilter.java`
- **Gateway Properties:** `src/main/resources/application.properties`
- **Profile Properties:** `src/main/resources/application-local.properties`, `application-docker.properties`, `application-gcp.properties`

## Current Route Configuration

Configured in `application.properties`:

- `/api/v1/tickets/**` -> `lb://TICKET-SERVICE`
- `/api/v1/analysis/**` -> `lb://AI-ANALYSIS-SERVICE`
- `/api/v1/routing/**` -> `lb://ROUTING-SERVICE`
- `/api/v1/rag/**` -> `lb://RAG-SERVICE`

## Key Responsibilities

1. Route requests to downstream services using Eureka + `lb://` URIs.
2. Ensure `X-Correlation-Id` is propagated for tracing.
3. Keep gateway focused on routing/filters only (no business logic).
4. Provide a stable external entry point for clients.

## Common Tasks

### Check Gateway Health
```bash
curl "http://localhost:8080/actuator/health"
```

### Create Ticket Through Gateway
```bash
curl -X POST "http://localhost:8080/api/v1/tickets" \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Payment failed",
    "message": "My card was declined",
    "customerEmail": "customer@example.com"
  }'
```

### Inspect Eureka Registration
```bash
curl "http://localhost:8761/eureka/apps"
```

## Important Rules

- Do not add business logic in gateway.
- Keep gateway reactive (WebFlux stack only).
- Do not hardcode downstream host/port URLs in routing.
- Preserve correlation-id header across the request chain.

## Environment Variables

- `SPRING_PROFILES_ACTIVE`
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`
- `SERVER_PORT`

## Related Services

- `discovery-service`
- `ticket-service`
- `ai-analysis-service`
- `routing-service`
- `rag-service`

## Debugging Tips

1. Route mismatch: verify `spring.cloud.gateway.routes[*].predicates` in properties.
2. Service not found: confirm Eureka registration for target service.
3. Missing correlation-id downstream: inspect `CorrelationIdFilter` and downstream filters/logs.
