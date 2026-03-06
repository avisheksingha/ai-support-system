# AI Analysis Service

Microservice that uses Spring AI and Google Vertex AI (Gemini) to analyze support tickets for sentiment, urgency, and intent.

## Features

- Analyzes ticket content (Subject + Message)
- Detects Sentiment (POSITIVE, NEUTRAL, NEGATIVE)
- Determines Urgency (LOW, MEDIUM, HIGH, CRITICAL)
- Identifies User Intent (TECHNICAL, BILLING, etc.)
- Auto-tagging with keywords
- Built on **Spring Boot 4.0.3** and **Spring AI Vertex AI Starter**

## Configuration

| Property | Value | Description |
| ---------- | ------- | ------------- |
| Server Port | 8083 | Port where service runs |
| AI Model | Gemini 1.5 Flash | Google Vertex AI via Spring AI |
| Database | PostgreSQL | `analysis_db` |
| Service Discovery | Enabled | Registers with Eureka |

> [!IMPORTANT]
> You must set your Google Cloud Project credentials and location in the environment variables or application properties.

### Environment Variables

- `GCP_PROJECT_ID`: Your Google Cloud Project ID.
- `GCP_LOCATION`: Your Google Cloud Project location (e.g., `us-central1`).
- `GOOGLE_APPLICATION_CREDENTIALS`: Path to your GCP service account JSON key file.

## API Endpoints

- `POST /api/v1/analysis/analyze`: Analyze a ticket manually
- `GET /api/v1/analysis/{ticketId}`: Get existing analysis

## Running Locally

1. Set up your Google Cloud default credentials via `gcloud auth application-default login` or set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
2. Run the application:

```bash
mvn spring-boot:run
```
