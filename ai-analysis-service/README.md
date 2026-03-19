# AI Analysis Service

Microservice that uses Spring AI to analyze support tickets for sentiment, urgency, and intent using multiple AI providers (Google Vertex AI / OpenAI).

## Features

- Analyzes ticket content (Subject + Message)
- Detects Sentiment (POSITIVE, NEUTRAL, NEGATIVE)
- Determines Urgency (LOW, MEDIUM, HIGH, CRITICAL)
- Identifies User Intent (TECHNICAL, BILLING, etc.)
- Auto-tagging with keywords
- Built on **Spring Boot 4.0.4** and **Spring AI**
- Supports multiple AI providers: **Google Gemini** and **OpenAI**

## Configuration

| Property | Value | Description |
| ---------- | ------- | ------------- |
| Server Port | 8083 | Port where service runs |
| AI Providers | Gemini 1.5 Flash, OpenAI (GPT-4o) | Supported AI models via Spring AI |
| Database | PostgreSQL | `analysis_db` |
| Service Discovery | Enabled | Registers with Eureka |

> [!IMPORTANT]
> You must set your AI provider credentials (GCP or OpenAI) in the environment variables or application properties.

### Environment Variables

#### Google Vertex AI (Gemini)
- `GCP_PROJECT_ID`: Your Google Cloud Project ID.
- `GCP_LOCATION`: Your Google Cloud Project location (e.g., `us-central1`).
- `GOOGLE_APPLICATION_CREDENTIALS`: Path to your GCP service account JSON key file.

#### OpenAI
- `SPRING_AI_OPENAI_API_KEY`: Your OpenAI API key.

## Interfaces & Endpoints

### Event-Driven (Kafka)
- **Consumes**: `TicketCreatedEvent` (Triggers AI analysis workflow). Extracts `X-Correlation-Id` from Kafka headers for tracing.
- **Produces**: `TicketAnalyzedEvent` (Publishes results for routing and RAG). Employs a **Resilient Outbox Pattern** with timeout/retry mechanisms to guarantee delivery.

### Observability
- All REST and Kafka interactions are traced via a `CorrelationIdFilter` binding to Logback's `MDC`.

### REST API
- `POST /api/v1/analysis/analyze`: Analyze a ticket manually
- `GET /api/v1/analysis/{ticketId}`: Get existing analysis

## Running Locally

1. Configure your AI provider credentials (see Environment Variables).
2. Run the application:

```bash
mvn spring-boot:run
```
