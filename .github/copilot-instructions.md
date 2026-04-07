# Copilot Instructions for AI Support System

This repository is a Spring Boot 4.0.5 microservices platform for AI-powered ticket management, using Spring AI with Google Gemini integration, service discovery, and event-driven automation via Kafka.

## Build, Test, and Lint Commands

- **Build all services:**
  ```bash
  mvn clean install
  ```

- **Build a single module:**
  ```bash
  mvn clean install -pl <module-name>
  ```

- **Run a service:**
  ```bash
  cd <service-dir> && mvn spring-boot:run
  ```

- **Run tests (all):**
  ```bash
  mvn test
  ```

- **SonarQube analysis:**
  ```bash
  mvn clean verify sonar:sonar
  ```

## High-Level Architecture

- **discovery-service:** Eureka registry for all microservices (Port: 8761).
- **api-gateway:** Central entry point built on Spring Cloud Gateway, handles CORS, and assigns `X-Correlation-Id` (Port: 8080).
- **ticket-service:** Core ticket management with strict state machine transitions (Port: 8082).
- **ai-analysis-service:** AI analysis via Spring AI (Gemini) (Port: 8083).
- **routing-service:** Rule-based team assignment and SLA (Port: 8084).
- **rag-service:** Retrieval-Augmented Generation for context-aware suggestions (pgvector) (Port: 8085).
- **common-library:** Shared DTOs, enums, events, constants — not a runnable service.

### Service Startup Order
1. `discovery-service` (Eureka)
2. `api-gateway`
3. Core services (can run in parallel):
   - `ticket-service`
   - `ai-analysis-service`
   - `routing-service`
   - `rag-service`

### API Documentation
Each core service exposes OpenAPI docs at `/swagger-ui/index.html` (e.g., http://localhost:8082/swagger-ui/index.html).

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.5 + Spring Framework 7.0
- **Microservices**: Spring Cloud 2025.1.0
- **AI Integration**: Spring AI 2.0.0-M1 (Vertex AI Gemini 2.0 Flash)
- **Database**: PostgreSQL + PGVector extension (vector embeddings)
- **Messaging**: Apache Kafka 4.1
- **Service Discovery**: Netflix Eureka
- **API Documentation**: SpringDoc OpenAPI 3.0 (Swagger UI at /swagger-ui/index.html)
- **Object Mapping**: MapStruct 1.6.3
- **Resilience**: Spring Cloud CircuitBreaker + Resilience4j

## Key Conventions

- **Architecture:** Layered (Controller → Service → Repository).
- **DI:** Constructor-based dependency injection only — never @Autowired field injection.
- **Shared Code:** Use `common-library` for DTOs, Enums, Kafka Topics/Groups, Events, and Constants.
- **JPA Entities:** Use @Getter/@Setter — never @Data (breaks JPA proxying). Always include @NoArgsConstructor.
- **Testing:** JUnit 5 + Mockito for unit tests; @SpringBootTest + Testcontainers for integration tests.
- **Communication:** Synchronous via Eureka/REST and Asynchronous via Kafka.
- **Event Publishing:** Use the **Outbox Pattern** with a dedicated scheduler and retry semantics to guarantee Kafka event delivery.
- **Observability:** Use `CorrelationIdFilter` to map `X-Correlation-Id` to `MDC` for Logback tracing in both REST controllers and Kafka consumers.
- **Discovery:** Eureka (no hardcoded URLs).

## Environment Variables

### AI Analysis Service
- **Google Cloud:** `GCP_PROJECT_ID`, `GCP_LOCATION`, `GOOGLE_APPLICATION_CREDENTIALS`.

## Important Rules

- Never bypass API Gateway to call services directly (use port 8080).
- Never share DB schemas between services.
- Never add @Transactional directly on Kafka consumer methods (use @Transactional on the service method called from the consumer).
- Never use spring-boot-starter-webmvc in api-gateway (WebFlux only).
- Never use spring-boot-starter-webflux in MVC services (servlet stack conflict).
- Never put @Entity classes in common-library.
- Never skip @NoArgsConstructor on JPA entities.
- Never skip @EnableScheduling on services that use @Scheduled.
- All Kafka events go through OutboxEvent entity — never direct KafkaTemplate.
- Outbox events: status PENDING/SENT/FAILED/DEAD, MAX_RETRIES=3.

## End-to-End Data Flow

1. Client sends POST /api/v1/tickets through API Gateway
2. API Gateway injects X-Correlation-Id header for distributed tracing
3. ticket-service creates ticket, saves TicketCreatedEvent to outbox table
4. OutboxEventPublisher polls every 2s, publishes to ticket-created topic
5. ai-analysis-service consumes event, calls Gemini API, publishes TicketAnalyzedEvent
6. routing-service and rag-service consume TicketAnalyzedEvent in parallel:
   - routing-service: matches DB rules, assigns team + SLA, publishes TicketRoutedEvent
   - rag-service: PGVector similarity search, Gemini RAG response, publishes TicketRagResponseEvent
7. ticket-service consumes TicketRoutedEvent and TicketRagResponseEvent
   — updates assignment, priority, SLA, and rag_response on the ticket

## Service-Specific Guidance

When working on code in a specific service, prioritize these tailored rules and context. Copilot should reference this section for targeted suggestions.

### discovery-service (Eureka Registry)
- Focus on Eureka server configuration and service registration.
- Avoid adding business logic; keep it lightweight.
- Startup: First in order (port 8761).

### api-gateway (Spring Cloud Gateway)
- Use WebFlux only; no MVC starters.
- Implement CorrelationIdFilter as a GlobalFilter for X-Correlation-Id injection.
- Handle CORS and routing; no direct service calls.

### ticket-service (Core Ticket Management)
- Enforce strict state machine transitions (e.g., via enums in common-library).
- Use outbox pattern for events; @EnableScheduling required.
- Controllers: Use service-specific DTOs (not common-library).
- Entities: @Getter/@Setter + @NoArgsConstructor; no @Data.

### ai-analysis-service (AI Analysis)
- Integrate Spring AI ChatClient for Gemini calls; wrap in try-catch.
- Publish TicketAnalyzedEvent after analysis (intent, sentiment, urgency).
- Environment vars: GCP_PROJECT_ID, GCP_LOCATION, GOOGLE_APPLICATION_CREDENTIALS.
- No DB writes; consume from Kafka.

### routing-service (Rule-Based Routing)
- Match DB rules for team assignment and SLA.
- Publish TicketRoutedEvent.
- Use constructor injection; @Transactional on service methods.

### rag-service (RAG Pipeline)
- Use PGVector for embeddings; QuestionAnswerAdvisor for RAG.
- DataLoaderRunner: Skip if already embedded.
- Publish TicketRagResponseEvent.
- AI model name via @Value.

### common-library (Shared Code)
- Only DTOs, enums, events, constants—no @Entity or beans.
- Used by all services for Kafka payloads and shared types.
- Not runnable; reference in other modules.

## Testing Expectations

- Unit tests: Mockito for service layer.
- Integration tests: @SpringBootTest + Testcontainers for PostgreSQL + Kafka.
- Always test happy path + at least one failure/edge case.
- Test state machine transitions including invalid transition exceptions.