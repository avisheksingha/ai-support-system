# AI Analysis Service

Microservice that uses Google Gemini AI to analyze support tickets for sentiment, urgency, and intent.

## Features
- Analyzes ticket content (Subject + Message)
- Detects Sentiment (POSITIVE, NEUTRAL, NEGATIVE)
- Determines Urgency (LOW, MEDIUM, HIGH, CRITICAL)
- Identifies User Intent (TECHNICAL, BILLING, etc.)
- Auto-tagging with keywords

## Configuration
**IMPORTANT**: You must set your Gemini API Key in the environment variables for security.

| Property | Default | Description |
|----------|---------|-------------|
| Server Port | 8083 | Port where service runs |
| AI Model | Gemini Pro | Google Generative AI |
| Database | PostgreSQL | `analysis_db` |

### Environment Variables
- `GEMINI_API_KEY`: Your Google Cloud Project API Key for Gemini.

## API Endpoints
- `POST /api/v1/analysis/analyze`: Analyze a ticket manually
- `GET /api/v1/analysis/{ticketId}`: Get existing analysis

## Running Locally
```bash
export GEMINI_API_KEY="your-api-key"
mvn spring-boot:run
```
