# RAG Service

The RAG (Retrieval-Augmented Generation) Service is a Domain Capability Service responsible for managing embeddings, vector storage, and providing contextual knowledge retrieval capabilities for the AI Support System. It acts as a context provider for the `ai-orchestration-service`.

## Features

- **Document Embedding**: Uses Spring AI and Google GenAI to generate embeddings for knowledge base articles or past tickets.
- **Vector Storage**: Stores embeddings in a PostgreSQL database using the `pgvector` extension.
- **Metadata-Aware Retrieval Engine**: Leverages hybrid ranking (vector similarity, category, tag, and title scoring) and dynamic fallback strategies to retrieve relevant documents and generate grounded, context-aware answers.
- **Separation of Concerns**: Encapsulates prompt construction via `RagPromptFactory` and JSON serialization via `SourceMetadataSerializer`, keeping service orchestration focused on core workflow execution.
- **Observability & Diagnostics**: Generates rich `RetrievalDiagnostics` (retrieval latencies, fallback usage, category/tag/keyword matches, and strategy tracking) for auditability and governance, alongside distributed log tracing via Logback MDC.
- **Interactions (REST & Kafka)**: Provides retrieval capabilities synchronously via REST (Tool Calling) for workflow execution, and listens to domain events via Kafka to trigger automated document indexing.

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
- Google Cloud credentials configured (`GOOGLE_CLOUD_PROJECT`, `GOOGLE_CLOUD_LOCATION`, and `GOOGLE_APPLICATION_CREDENTIALS` or Application Default Credentials via `gcloud auth application-default login`).

## API Endpoints

### Knowledge Article Management (`/api/internal/rag/articles`)

These endpoints are consumed by `ai-orchestration-service` and are not routed through the API Gateway.

- `POST /search`: Search for articles by query text.
- `GET /{id}`: Retrieve a single article by ID.
- `POST /`: Create a new knowledge article.
- `PUT /{id}`: Update an existing article.
- `DELETE /{id}`: Delete an article.
- `POST /sync-embeddings`: Trigger embedding synchronization for articles.
- `GET /stats`: Retrieve knowledge base statistics.
- `POST /bulk-publish`: Publish multiple articles in bulk.

### Internal RAG Retrieval (`/api/internal/rag`)

- `POST /search`: Contextual retrieval query used by the orchestrator during workflow execution.
- `GET /ticket/{ticketId}`: Retrieve existing RAG context for a ticket.

### Admin Stats (`/api/internal/rag`)

- `GET /stats/admin`: Retrieve admin-level RAG statistics for the dashboard.

### Kafka Consumer

The service consumes `ticket-analyzed` events to trigger automated document indexing and knowledge retrieval workflows.

## Running Locally

```bash
mvn spring-boot:run
```
