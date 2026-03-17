# Infrastructure Setup

This directory contains the necessary configuration files to spin up the required infrastructure backing the AI Support System, specifically using Docker Compose.

## Components

- **PostgreSQL**: The primary relational database used by `ticket-service` and `ai-analysis-service`.
- **pgvector**: An extension for PostgreSQL required by the `rag-service` for vector similarity search.
- **Kafka / Zookeeper**: For event-driven messaging and asynchronous communication between services.

## Files

- `docker-compose.yml`: Defines the services, volumes, and networks.
- `init.sql`: Initialization script to set up databases and schemas on the first run.

## Setup Instructions

1. Ensure Docker and Docker Compose are installed on your machine.
2. Run the following command to start all infrastructure components:

```bash
docker-compose up -d
```

3. To stop the infrastructure, run:

```bash
docker-compose down
```
