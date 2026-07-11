# RAG Service

The RAG (Retrieval-Augmented Generation) Service is a Domain Capability Service responsible for managing embeddings, vector storage, and providing contextual knowledge retrieval capabilities for the AI Support System. It acts as a context provider for the `ai-orchestration-service`.

## Features

- **Document Embedding**: Uses Spring AI and Google GenAI to generate embeddings for knowledge base articles or past tickets.
- **Vector Storage**: Stores embeddings in a PostgreSQL database using the `pgvector` extension.
- **Contextual Retrieval**: Leverages the `QuestionAnswerAdvisor` pattern to retrieve relevant documents and generate accurate, context-aware answers.
- **Interactions (REST & Kafka)**: Provides retrieval capabilities synchronously via REST (Tool Calling) for workflow execution, and listens to domain events via Kafka to trigger automated document indexing.
- **Observability**: Utilizes a `CorrelationIdFilter` and Kafka Header extraction to ensure distributed log tracing via Logback MDC.

## Technology Stack

- **Spring AI**: For AI model integration and Vector Store abstraction.
- **Google GenAI**: For both Chat Models and Embedding Models.
- **PostgreSQL + pgvector**: For efficient similarity search.

## Configuration

| Property | Value | Description |
| ---------- | ------- | ------------- |
| Server Port | 8085 | Port where service runs |
| Vector Store | PostgreSQL | Requires `pgvector` extension |
| Service Discovery | Enabled | Registers with Eureka |

## Prerequisites

- PostgreSQL with `pgvector` installed.
- Appropriate Google GenAI credentials configured (API Key or Google Cloud credentials).

## Running Locally

```bash
mvn spring-boot:run
```
