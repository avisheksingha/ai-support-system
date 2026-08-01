# System Overview

## High-Level System Architecture

The AI Support System is built using a microservices architecture pattern. This approach allows independent scaling, development, and deployment of distinct business capabilities. Each microservice is designed to be loosely coupled, communicating primarily through RESTful APIs and asynchronous messaging via Apache Kafka. The system is structured around a central API Gateway that serves as the single entry point for all client interactions, ensuring consistent request handling and traceability across services.

![Architecture Diagram](docs/architecture/architecture-diagram.svg)

### Core Components

1. **Client / API Gateway (`api-gateway`)**: Built iteratively on Spring Cloud Gateway WebFlux, serving as the single entry point. It handles request tracing by generating a unique `X-Correlation-Id` for every incoming request and routing them to the appropriate backend microservice.
2. **Service Registry (`discovery-service`)**: Utilizes Netflix Eureka to allow microservices to register themselves and discover each other dynamically.
3. **Authentication (`auth-service`)**: Manages user authentication, authorization, and issues JWTs for secure access across the microservices.
4. **Ticket Management (`ticket-service`)**: Handles the core CRUD operations for support tickets, applying state machine validations for status transitions, and persists data to PostgreSQL.
5. **AI Workflow Runtime (`ai-orchestration-service`)**: The central orchestrator that consumes events and coordinates the AI execution lifecycle.
6. **AI Processing (`ai-analysis-service`)**: Domain capability providing sentiment analysis, urgency detection, and intent extraction via Google GenAI.
7. **Intelligent Routing (`routing-service`)**: Domain capability providing deterministic ticket routing decisions based on AI tags.
8. **Knowledge Context (`rag-service`)**: Domain capability providing intelligent, context-aware responses via `pgvector`.
9. **AI Support Marketplace (`ai-support-marketplace`)**: A plugin and tooling ecosystem that extends development capabilities with agents, hooks, and commands.

## Module Interactions and Dependencies

The system employs both synchronous and asynchronous communication:

* **Synchronous (REST)**: Handled primarily through the API Gateway for external requests, or directly via Eureka service discovery for direct service-to-service queries (e.g., fetching ticket details). All synchronized requests are logged with a traceable `X-Correlation-Id`.
* **Asynchronous (Event-Driven)**: Handled via Apache Kafka. When a significant domain event occurs (such as a ticket being created), an event is reliably published to a Kafka topic leveraging a robust **Outbox Pattern** with retry semantics. The `ai-orchestration-service` reacts to these business events and manages the asynchronous workflow execution, extracting the correlation ID directly from Kafka headers for consistent lifecycle tracing.

## Diagrams

### 1. High-Level Architecture Flowchart

```mermaid
graph TD
    Client[Client Apps / Web] -->|HTTP/REST| API_GW[API Gateway<br>:8080]
    
    API_GW -->|Route| AUTH[Auth Service<br>:8081]
    API_GW -->|Route| TS[Ticket Service<br>:8082]
    API_GW -->|Route| ORCH[AI Orchestrator<br>:8086]
    API_GW -->|Route| AIS[AI Analysis<br>:8083]
    API_GW -->|Route| RS[Routing<br>:8084]
    API_GW -->|Route| RAG[RAG Service<br>:8085]

    DS[Discovery Service<br>:8761] -.->|Service Registry| API_GW
    DS -.->|Registers| AUTH
    DS -.->|Registers| TS
    DS -.->|Registers| ORCH
    DS -.->|Registers| AIS
    DS -.->|Registers| RS
    DS -.->|Registers| RAG

    TS <-->|Reads/Writes| DB[(PostgreSQL<br>ticket_db)]
    ORCH <-->|Reads/Writes| ORCH_DB[(PostgreSQL<br>orchestrator_db)]

    TS -->|Publishes TicketCreated| Kafka[Apache Kafka<br>Message Broker]
    Kafka -->|Consumes TicketCreated| ORCH
    
    ORCH -.->|Sync REST| AIS
    ORCH -.->|Sync REST| RS
    ORCH -.->|Sync REST| RAG

    AIS -->|Calls API| ExternalAI[Google GenAI / OpenAI]
    RAG -->|Calls API| ExternalAI
    RAG <-->|Vector Search| PGV[(PostgreSQL + pgvector)]
    
    ORCH -->|Publishes TicketOrchestratedEvent| Kafka
```

### 2. Sequence Diagram: Ticket Creation & AI Routing

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant TicketSvc as Ticket Service
    participant Kafka as Apache Kafka
    participant OrchSvc as AI Orchestration Service
    participant AISvc as AI Analysis Service
    participant RoutingSvc as Routing Service
    participant RAGSvc as RAG Service
    participant ExternalAI as Google GenAI

    Client->>Gateway: POST /api/v1/tickets
    Gateway->>TicketSvc: Forward Request
    TicketSvc->>TicketSvc: Save Ticket to DB (Status: OPEN)
    TicketSvc-->>Gateway: 201 Created (Ticket ID)
    Gateway-->>Client: 201 Created
    
    TicketSvc->>Kafka: Publish "TicketCreatedEvent"
    Kafka-->>OrchSvc: Consume "TicketCreatedEvent"
    
    OrchSvc->>OrchSvc: Initialize Workflow Runtime
    
    opt Synchronous Composition (Tool Calling)
        OrchSvc->>AISvc: Analyze Ticket Data (REST)
        AISvc->>ExternalAI: Generate Sentiment, Urgency
        ExternalAI-->>AISvc: Results
        AISvc-->>OrchSvc: Extracted Tags
        
        OrchSvc->>RAGSvc: Retrieve Context (REST)
        RAGSvc->>ExternalAI: Embed & Search pgvector
        RAGSvc-->>OrchSvc: Contextual Suggestions
        
        OrchSvc->>RoutingSvc: Evaluate Rules (REST)
        RoutingSvc-->>OrchSvc: Recommended Queue
    end
    
    OrchSvc->>TicketSvc: PATCH /api/v1/tickets/{id}/assign (Apply Updates)
    OrchSvc->>Kafka: Publish "TicketOrchestratedEvent"
    Kafka-->>TicketSvc: Consume "TicketOrchestratedEvent"
    TicketSvc->>TicketSvc: Apply AI analysis, routing decision, and knowledge context
```
