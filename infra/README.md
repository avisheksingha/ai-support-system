# Infrastructure Setup

This directory contains the infrastructure-only Docker Compose setup for the AI Support System.

## Components

- **PostgreSQL + pgvector**: Databases for all backend services, including `rag-service`.
- **Kafka / Zookeeper**: Event streaming backbone for asynchronous service communication.

> [!NOTE]
> This compose file is intentionally **infra-only**. Application services (`discovery-service`, `api-gateway`, `ticket-service`, `ai-analysis-service`, `routing-service`, `rag-service`) are started separately via Eclipse or Maven.

## Files

- `docker-compose.yml`: Defines the services, volumes, and networks.
- `init.sql`: Initialization script to set up databases and schemas on the first run.

## Setup Instructions

1. Ensure Docker and Docker Compose are installed on your machine.
2. From the repository root, run the following command to start all infrastructure components:

```bash
docker compose -f infra/docker-compose.yml up -d
```

3. To stop the infrastructure, run:

```bash
docker compose -f infra/docker-compose.yml down
```
