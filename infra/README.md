# Infrastructure Setup

This directory contains the infrastructure-only Docker Compose setup for the AI Support System.

## Components

- **PostgreSQL + pgvector**: Databases for all backend services, including `rag-service`.
- **Kafka / Zookeeper**: Event streaming backbone for asynchronous service communication.

> [!NOTE]
> This compose file is intentionally **infra-only**. Application services (`discovery-service`, `api-gateway`, `auth-service`, `ticket-service`, `ai-analysis-service`, `routing-service`, `rag-service`) are started separately via Eclipse or Maven.

## Files

- `docker-compose.yml`: Defines the services, volumes, and networks.
- `init.sql`: Initialization script to set up databases and schemas on the first run.

## Setup Instructions

Please refer to the **Local Development** section in the [root README](../README.md) for complete instructions on starting, stopping, and resetting the Docker infrastructure.
