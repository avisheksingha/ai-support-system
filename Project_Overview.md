# Project Overview: AI Support System

## 1. Introduction
The AI Support System is a modern, microservices-based platform designed to automate and augment traditional support ticket management. It uses Artificial Intelligence to analyze tickets for sentiment, urgency, and intent, and intelligently routes them to the appropriate human agents or teams. The platform incorporates a RAG (Retrieval-Augmented Generation) engine to provide suggestions and automatically resolve common queries.

## 2. Architecture & Modules
The system is composed of several independent microservices communicating synchronously via REST (through an API Gateway) and asynchronously via Apache Kafka.

### Core Modules
* **`discovery-service`**: A Netflix Eureka server providing service registry and discovery. Allows services to locate each other dynamically.
* **`api-gateway`**: Built with Spring Cloud Gateway. It serves as the single entry point for all client requests, routing them to the correct backend service.
* **`ticket-service`**: The core CRUD service for managing the lifecycle of support tickets. It persists data to PostgreSQL and publishes domain events to Kafka.
* **`ai-analysis-service`**: Listens for new tickets, calls AI models (Google Vertex AI / OpenAI) to analyze sentiment and urgency, and updates ticket metadata.
* **`routing-service`**: Orchestrates the complex routing workflows. It consumes AI analysis results and applies business rules to assign tickets to the correct team.
* **`rag-service`**: Manages a vector database (via `pgvector`) of historical data/knowledge base to generate contextually relevant responses to newly submitted tickets.

### Shared Modules
* **`common-library`**: A shared JAR containing common DTOs, Event models, and utility classes to ensure consistency across the services.
* **`aisupport-parent`**: The parent Maven POM that centralizes dependency versions (Spring Boot 4.0.3, Spring Cloud 2025.1.0, Spring AI 2.0.0-M1).
* **`infra`**: Contains Docker Compose configuration to spin up the necessary infrastructure dependencies (PostgreSQL, Kafka).

## 3. System Flow & Interactions

### Standard Ticket Creation Flow
1. **Client Request**: A client sends a POST request to `/api/v1/tickets` via the `api-gateway`.
2. **API Gateway**: Routes the request to the `ticket-service`.
3. **Ticket Service**: Creates the ticket in the PostgreSQL database in an `OPEN` state. It immediately publishes a `TicketCreatedEvent` to a Kafka topic.
4. **AI Analysis Service**: Subscribes to the Kafka topic. Upon receiving the `TicketCreatedEvent`, it:
   - Queries the AI provider (e.g., Gemini) with the ticket content.
   - Determines sentiment (e.g., NEGATIVE), urgency (e.g., HIGH), and intent.
   - Calls the `ticket-service` REST API (or publishes an event) to update the ticket with these tags.
5. **Routing Service**: Can be triggered concurrently or sequentially. It evaluates the AI tags and applies logic (e.g., "If Urgency is HIGH and Intent is BILLING, route to the Tier-2 Billing Team").
6. **RAG Service (Optional Flow)**: Monitors incoming tickets and uses similarity search against the knowledge base to suggest a response to the agent.

## 4. Dependencies Map
- **Databases**: Both `ticket-service` and `ai-analysis-service` depend on PostgreSQL. `rag-service` requires the `pgvector` extension.
- **Messaging**: Services communicate via Apache Kafka.
- **AI Models**: `ai-analysis-service` and `rag-service` depend on external APIs (Google Cloud Vertex AI or OpenAI).

## 5. Deployment & Execution
To run the system locally, infrastructure dependencies (DB, Kafka) must first be started via `infra/docker-compose.yml`. Then, the Java microservices should be started, beginning with `discovery-service` and followed by the others.
