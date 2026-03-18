# RAG Service

The RAG (Retrieval-Augmented Generation) Service is responsible for managing embeddings, vector storage, and providing context-aware AI responses for the AI Support System.

## Features

- **Document Embedding**: Uses Spring AI and Google Vertex AI to generate embeddings for knowledge base articles or past tickets.
- **Vector Storage**: Stores embeddings in a PostgreSQL database using the `pgvector` extension.
- **Contextual Generation**: Leverages the `QuestionAnswerAdvisor` pattern to retrieve relevant documents and generate accurate, context-aware answers.
- **Kafka Integration**: Listens to domain events to trigger automated document indexing.
- **Observability**: Utilizes a `CorrelationIdFilter` and Kafka Header extraction to ensure distributed log tracing via Logback MDC.

## Technology Stack

- **Spring AI**: For AI model integration and Vector Store abstraction.
- **Google Vertex AI**: For both Chat Models (Gemini) and Embedding Models.
- **PostgreSQL + pgvector**: For efficient similarity search.

## Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8085 | Port where service runs |
| Vector Store | PostgreSQL | Requires `pgvector` extension |
| Service Discovery | Enabled | Registers with Eureka |

## Prerequisites

- PostgreSQL with `pgvector` installed.
- Appropriate Vertex AI credentials configured (e.g., `GOOGLE_APPLICATION_CREDENTIALS`).

## Running Locally

```bash
mvn spring-boot:run
```
