# AI Analysis Service

Microservice that uses Spring AI to analyze support tickets for sentiment, urgency, and intent. The active provider is Google Vertex AI (Gemini), with OpenAI available as an optional provider.

## Features

- Analyzes ticket content (Subject + Message)
- Detects Sentiment (POSITIVE, NEUTRAL, NEGATIVE)
- Determines Urgency (LOW, MEDIUM, HIGH, CRITICAL)
- Identifies User Intent (TECHNICAL, BILLING, etc.)
- Auto-tagging with keywords
- Built on **Spring Boot 4.0.5** and **Spring AI**
- Active AI provider: **Google Vertex AI (Gemini)**
- Optional provider support: **OpenAI**

## Configuration

| Property | Value | Description |
| ---------- | ------- | ------------- |
| Server Port | 8083 | Port where service runs |
| AI Providers | Vertex AI Gemini (active), OpenAI (optional) | Supported provider options via Spring AI |
| Database | PostgreSQL | `analysis_db` |
| Service Discovery | Enabled | Registers with Eureka |

> [!IMPORTANT]
> Set Vertex AI credentials for standard runs. OpenAI credentials are only needed if you switch provider.

### Environment Variables

#### Google Vertex AI (Gemini)
- `GCP_PROJECT_ID`: Your Google Cloud Project ID.
- `GCP_LOCATION`: Your Google Cloud Project location (e.g., `us-central1`).
- `GOOGLE_APPLICATION_CREDENTIALS`: Path to your GCP service account JSON key file.

#### OpenAI (Optional)
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

1. Configure Vertex AI credentials (OpenAI is optional).
2. Run the application:

```bash
mvn spring-boot:run
```
