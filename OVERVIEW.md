# System Overview

## High-Level System Architecture

The AI Support System is built using a microservices architecture pattern. This approach allows independent scaling, development, and deployment of distinct business capabilities. 

### Core Components

1. **Client / API Gateway (`api-gateway`)**: Serves as the single entry point for all incoming requests, routing them to the appropriate backend microservice.
2. **Service Registry (`discovery-service`)**: Utilizes Netflix Eureka to allow microservices to register themselves and discover each other dynamically.
3. **Ticket Management (`ticket-service`)**: Handles the core CRUD operations for support tickets and persists data to PostgreSQL.
4. **AI Processing (`ai-analysis-service`)**: Integrates with Google Vertex AI or OpenAI to perform sentiment analysis, determine urgency, and extract user intent from ticket content.
5. **Intelligent Routing (`routing-service`)**: Applies business rules to the AI-generated tags to route tickets to specific agents or queues.
6. **Knowledge Context (`rag-service`)**: A Retrieval-Augmented Generation service that queries a vector database (`pgvector`) to provide intelligent, context-aware responses and suggestions.

## Module Interactions and Dependencies

The system employs both synchronous and asynchronous communication:

*   **Synchronous (REST)**: Handled primarily through the API Gateway for external requests, or directly via Eureka service discovery for direct service-to-service queries (e.g., fetching ticket details).
*   **Asynchronous (Event-Driven)**: Handled via Apache Kafka. When a significant domain event occurs (such as a ticket being created), an event is published to a Kafka topic. Services like `ai-analysis-service` and `routing-service` react to these events without tight coupling.

## Diagrams

### 1. High-Level Architecture Flowchart

```mermaid
graph TD
    Client[Client Apps / Web] -->|HTTP/REST| API_GW[API Gateway<br>:8081]
    
    API_GW -->|Route| TS[Ticket Service<br>:8082]
    API_GW -->|Route| AIS[AI Analysis Service<br>:8083]
    API_GW -->|Route| RS[Routing Service<br>:8084]
    API_GW -->|Route| RAG[RAG Service<br>:8085]

    DS[Discovery Service<br>:8761] -.->|Service Registry| API_GW
    DS -.->|Registers| TS
    DS -.->|Registers| AIS
    DS -.->|Registers| RS
    DS -.->|Registers| RAG

    TS <-->|Reads/Writes| DB[(PostgreSQL<br>ticket_db)]
    AIS <-->|Reads/Writes| DB

    TS -->|Publishes TicketCreated| Kafka[Apache Kafka<br>Message Broker]
    Kafka -->|Consumes TicketCreated| AIS
    AIS -->|Calls API| ExternalAI[Google Vertex AI / OpenAI]
    AIS -->|Publishes TicketAnalyzed| Kafka
    Kafka -->|Consumes TicketAnalyzed| RS
    Kafka -->|Consumes TicketAnalyzed| RAG
    
    RAG <-->|Vector Search| PGV[(PostgreSQL + pgvector)]
    RAG -->|Calls API| ExternalAI
```

### 2. Sequence Diagram: Ticket Creation & AI Routing

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant TicketSvc as Ticket Service
    participant Kafka as Apache Kafka
    participant AISvc as AI Analysis Service
    participant ExternalAI as Vertex AI / OpenAI
    participant RoutingSvc as Routing Service
    participant RAGSvc as RAG Service

    Client->>Gateway: POST /api/v1/tickets
    Gateway->>TicketSvc: Forward Request
    TicketSvc->>TicketSvc: Save Ticket to DB (Status: OPEN)
    TicketSvc-->>Gateway: 201 Created (Ticket ID)
    Gateway-->>Client: 201 Created
    
    TicketSvc->>Kafka: Publish "TicketCreatedEvent"
    Kafka-->>AISvc: Consume "TicketCreatedEvent"
    
    AISvc->>ExternalAI: Send Ticket Text for Analysis
    ExternalAI-->>AISvc: Return Sentiment, Urgency, Intent
    AISvc->>TicketSvc: PATCH /api/v1/tickets/{id}/status (Update AI Tags)
    AISvc->>Kafka: Publish "TicketAnalyzedEvent"
    
    par AI Routing Workflow
        Kafka-->>RoutingSvc: Consume "TicketAnalyzedEvent"
        RoutingSvc->>RoutingSvc: Evaluate Rules based on AI Tags
        RoutingSvc->>TicketSvc: PATCH /api/v1/tickets/{id}/assign (Assign Agent/Queue)
    and RAG Suggestion Workflow
        Kafka-->>RAGSvc: Consume "TicketAnalyzedEvent"
        RAGSvc->>ExternalAI: Generate Embeddings & Search VectorDB
        ExternalAI-->>RAGSvc: Context/Similar Articles
        RAGSvc->>ExternalAI: Prompt QuestionAnswerAdvisor
        ExternalAI-->>RAGSvc: Context-Aware Suggestion
        RAGSvc->>TicketSvc: Add AI Suggestion to Ticket
    end
```
