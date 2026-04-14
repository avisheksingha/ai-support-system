# Copilot Instructions for AI Support System

This repository is a Spring Boot 4.0.5 microservices platform for AI-powered ticket management, using Spring AI with Gemini/OpenAI providers, service discovery, and event-driven workflows via Kafka.

## Build, Test, and Lint Commands

### Build

- **Build all modules:**
  ```bash
  mvn -f aisupport-parent/pom.xml clean install
  ```

- **Build a single module:**
  ```bash
  mvn -pl <module-name> clean install
  ```

- **Build without tests:**
  ```bash
  mvn -f aisupport-parent/pom.xml clean install -DskipTests
  ```

### Run Tests

- **Run all tests from repo root:**
  ```bash
  mvn test
  ```

- **Run tests for a single module:**
  ```bash
  mvn -pl <module-name> test
  ```

- **Run a specific test class:**
  ```bash
  mvn -pl <module-name> -Dtest=<TestClassName> test
  ```

- **Core test packs by service:**
  - **Ticket Service:** `mvn -pl ticket-service -Dtest=TicketControllerTest,TicketServiceBehaviorTest,GlobalExceptionHandlerTest,OutboxEventPublisherTest test`
  - **AI Analysis:** `mvn -pl ai-analysis-service -Dtest=AnalysisControllerTest,AnalysisProcessingServiceTest,AnalysisQueryServiceTest test`
  - **Routing:** `mvn -pl routing-service -Dtest=RoutingServiceTest,RuleEvaluationServiceTest test`
  - **RAG:** `mvn -pl rag-service -Dtest=RagServiceTest test`

### Run Services

- **Run a service:**
  ```bash
  cd <service-dir> && mvn spring-boot:run
  ```

- **Run with profile:**
  ```bash
  cd <service-dir> && mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=<profile>"
  ```

### Code Quality

- **SonarQube analysis:**
  ```bash
  mvn clean verify sonar:sonar
  ```

- **Generate Javadoc:**
  ```bash
  mvn javadoc:javadoc
  ```

## High-Level Architecture

- **discovery-service:** Eureka registry (Port: 8761)
- **api-gateway:** Spring Cloud Gateway entry point and correlation-id propagation (Port: 8080)
- **ticket-service:** Ticket lifecycle, updates, and outbox publishing (Port: 8082)
- **ai-analysis-service:** Consumes `ticket-created`, runs AI analysis, publishes `ticket-analyzed` (Port: 8083)
- **routing-service:** Consumes `ticket-analyzed`, evaluates DB rules, publishes `ticket-routed` (Port: 8084)
- **rag-service:** Consumes `ticket-analyzed`, runs RAG with PGVector, publishes `ticket-rag-response` (Port: 8085)
- **common-library:** Shared DTOs, enums, events, constants

### Service Startup Order
1. `discovery-service`
2. `api-gateway`
3. Core services (parallel): `ticket-service`, `ai-analysis-service`, `routing-service`, `rag-service`

### API Documentation
- Services with REST controllers expose Swagger UI at `/swagger-ui.html`.
- Examples:
  - http://localhost:8082/swagger-ui.html
  - http://localhost:8083/swagger-ui.html

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4.0.5 + Spring Framework 7
- **Microservices:** Spring Cloud 2025.1.0
- **AI Integration:** Spring AI 2.0.0-M1
- **Database:** PostgreSQL + PGVector
- **Messaging:** Apache Kafka
- **Service Discovery:** Netflix Eureka
- **API Documentation:** SpringDoc OpenAPI 3.0.x
- **Object Mapping:** MapStruct 1.6.3
- **Resilience:** Resilience4j

## Key Conventions

### Dependency Injection & Mapping
- Use constructor injection (`@RequiredArgsConstructor`) over field injection.
- Prefer explicit Lombok annotations on entities (`@Getter/@Setter`, `@NoArgsConstructor`) unless module already uses an established pattern.
- Use MapStruct with `componentModel = "spring"` where mapper beans are required.

### REST & Service Layer
- Keep controller DTOs service-specific.
- Use `@RestControllerAdvice` per service for domain-level error mapping.
- Keep transactional boundaries in service layer.

### Event-Driven Communication
- Use outbox flow for cross-service event publication.
- Keep scheduler-based outbox publishers enabled where present (`@Scheduled(fixedDelay = 2000)`).
- Preserve `X-Correlation-Id` in Kafka headers and restore it into MDC in consumers.

### API Gateway Rules
- `api-gateway` is reactive (WebFlux). Do not introduce MVC stack there.
- Other services should remain servlet-based.
- External client entry point is gateway on port `8080`.

### AI & RAG
- AI analysis uses pluggable providers (`chat.provider=gemini|openai`) via `ChatProvider`.
- RAG uses `QuestionAnswerAdvisor` + PGVector via Spring AI vector store.
- Model names and provider values come from config properties, not hardcoded literals.

### Correlation ID & Observability
- Gateway injects/passes `X-Correlation-Id`.
- Services populate MDC via `CorrelationIdFilter` (HTTP) and Kafka consumers (event path).
- Use `%X{correlationId:-no-correlation-id}` in log patterns.

## Environment Variables

### Common
- `SPRING_PROFILES_ACTIVE` (`local`, `docker`, `gcp`)
- `DB_USERNAME`, `DB_PASSWORD`
- `GOOGLE_APPLICATION_CREDENTIALS`
- `GCP_PROJECT_ID`, `GCP_LOCATION`
- `OPENAI_API_KEY` (if `chat.provider=openai`)

### Kafka
- Local profiles use `spring.kafka.bootstrap-servers=localhost:29092`.

## Project Structure Quick Reference

```text
api-gateway/          # Spring Cloud Gateway
/discovery-service/   # Eureka server
/ticket-service/      # Ticket APIs + outbox + consumers
/ai-analysis-service/ # Analysis consumer + query APIs + outbox
routing-service/      # Rule evaluation + outbox (event-driven)
rag-service/          # RAG generation + vector loading + outbox (event-driven)
common-library/       # Shared events/constants/enums/dtos
aisupport-parent/     # Maven parent pom
infra/                # docker-compose and init scripts
```

## Important Rules

- Do not bypass gateway for external traffic patterns.
- Do not publish integration events directly from business services without outbox persistence.
- Do not put entities in `common-library`.
- Do not add WebFlux dependencies to servlet services.
- Do not add MVC dependencies to `api-gateway`.

## End-to-End Event Flow

1. `ticket-service` creates ticket and writes `TicketCreatedEvent` to outbox.
2. Outbox publisher emits to topic `ticket-created`.
3. `ai-analysis-service` consumes, analyzes, and writes `TicketAnalyzedEvent` to outbox.
4. `routing-service` and `rag-service` consume `ticket-analyzed` in parallel.
5. `routing-service` emits `ticket-routed`; `rag-service` emits `ticket-rag-response`.
6. `ticket-service` consumers update ticket assignment/priority/SLA and rag response.

## Common Tasks

### Setup
```bash
docker compose -f infra/docker-compose.yml up -d
mvn -f aisupport-parent/pom.xml clean install
```

### Debug Kafka Flow
```bash
kafka-console-consumer --bootstrap-server localhost:29092 --topic ticket-created --from-beginning
curl http://localhost:8761/eureka/apps
```

## References

- `README.md`
- `ARCHITECTURE.md`
- `TESTING.md`
- `.github/agents/README.md`
