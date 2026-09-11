# Infrastructure Setup

This directory contains the infrastructure-only Docker Compose setup for the AI Support System.

## Components

- **PostgreSQL + pgvector**: Databases for all backend services, including `rag-service`.
- **Kafka (KRaft mode)**: Event streaming backbone for asynchronous service communication.
- **Redpanda Console**: Web UI for monitoring Kafka topics, consumer groups, and messages.

> [!NOTE]
> This compose file is intentionally **infra-only**. Application services (`discovery-service`, `api-gateway`, `auth-service`, `ticket-service`, `ai-analysis-service`, `routing-service`, `rag-service`, `ai-orchestration-service`) are started separately via an IDE or Maven. The React dashboard is started separately with npm.

## Services

| Service          |  Port | Image / Version               | Purpose                              |
| :--------------- | ----: | :---------------------------- | :----------------------------------- |
| PostgreSQL       |  5433 | `ankane/pgvector:latest`      | Relational + vector database storage |
| Kafka            | 29092 | `confluentinc/cp-kafka:8.3.1` | Event streaming (host access, KRaft) |
| Kafka (internal) |  9092 | `confluentinc/cp-kafka:8.3.1` | Event streaming (Docker network)     |
| Redpanda Console |  9090 | `redpandadata/console:latest` | Kafka monitoring UI                  |

## Files

- `docker-compose.yml`: Defines the services, volumes, and networks.
- `init.sql`: Initialization script to set up databases and schemas on the first run.

## Setup Instructions

Please refer to the **Local Development** section in the [root README](../README.md) for complete instructions on starting, stopping, and resetting the Docker infrastructure.
