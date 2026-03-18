# Copilot Instructions for AI Support System

This repository is a Spring Boot 4.0.3 microservices platform for AI-powered ticket management, using Spring AI with Google Gemini and OpenAI integration, service discovery, and event-driven automation via Kafka.

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

- **discovery-service:** Eureka registry for all microservices.
- **api-gateway:** Central entry point built on Spring Cloud Gateway **WebFlux**, handles CORS, and assigns `X-Correlation-Id` (Port: 8081).
- **ticket-service:** Core ticket management with strict state machine transitions (Port: 8082).
- **ai-analysis-service:** AI analysis via Spring AI (Gemini & OpenAI) (Port: 8083).
- **routing-service:** Orchestrates ticket routing (Port: 8084).
- **rag-service:** Retrieval-Augmented Generation for context-aware suggestions (pgvector) (Port: 8085).
- **common-library:** Shared DTOs, Kafka events (`TicketCreatedEvent`, etc.), exceptions, and constants (`KafkaTopics`, `TicketStatus`).
- **aisupport-parent:** Shared Maven parent configuration.

### Service Startup Order
1. `discovery-service` (Eureka)
2. `api-gateway`
3. Core services: `ticket-service`, `ai-analysis-service`, `routing-service` (any order)

### API Documentation
Each core service exposes OpenAPI docs at `/swagger-ui.html` (e.g., http://localhost:8082/swagger-ui.html).

## Key Conventions

- **Architecture:** Layered (Controller → Service → Repository).
- **DI:** Constructor-based dependency injection.
- **Shared Code:** Use `common-library` for DTOs, Enums, Kafka Topics/Groups, and Events.
- **Testing:** JUnit 5 + Mockito.
- **Communication:** Synchronous via Eureka/REST and Asynchronous via Kafka.
- **Event Publishing:** Use the **Outbox Pattern** with a dedicated scheduler and retry semantics to guarantee Kafka event delivery.
- **Observability:** Use `CorrelationIdFilter` to map `X-Correlation-Id` to `MDC` for Logback tracing in both REST controllers and Kafka consumers.
- **Discovery:** Eureka (no hardcoded URLs).

## Environment Variables

### AI Analysis Service
- **Google Cloud:** `GCP_PROJECT_ID`, `GCP_LOCATION`, `GOOGLE_APPLICATION_CREDENTIALS`.
- **OpenAI:** `SPRING_AI_OPENAI_API_KEY`.

---

Summary: Updated .github/copilot-instructions.md with the latest tech stack (Java 21, Spring Boot 4.x), OpenAI/Gemini integration details, MDC Tracing, WebFlux Gateway, Outbox pattern rules, and rag-service integration.
