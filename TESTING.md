# Testing Guide

This guide provides the fastest way to run the most relevant tests for this project.

## Prerequisites

- Java 21
- Maven Wrapper (`mvnw.cmd`) is already included in each module
- `MAVEN_USER_HOME` configured (example: `<path-to-m2-repo>`)

## Windows Troubleshooting (Only If Maven Fails to Start)

If `MAVEN_OPTS` is accidentally set to a raw path (for example `<path-to-m2-repo>`), Maven can fail to start.

Use this before running commands in the same PowerShell session:

```powershell
Remove-Item Env:MAVEN_OPTS -ErrorAction SilentlyContinue
```

## 1. Install Parent and Shared Library

From repo root:

```powershell
.\aisupport-parent\mvnw.cmd -B -ntp -f aisupport-parent\pom.xml -N install
.\common-library\mvnw.cmd -B -ntp -f common-library\pom.xml clean install -DskipTests
```

## 2. Run Core Controller/Service Test Pack

### Ticket Service

```powershell
.\ticket-service\mvnw.cmd -B -ntp -f ticket-service\pom.xml "-Dtest=TicketControllerTest,TicketServiceBehaviorTest,GlobalExceptionHandlerTest,OutboxEventPublisherTest" test
```

### AI Analysis Service

```powershell
.\ai-analysis-service\mvnw.cmd -B -ntp -f ai-analysis-service\pom.xml "-Dtest=AnalysisControllerTest,AnalysisProcessingServiceTest,AnalysisQueryServiceTest" test
```

### Routing Service

```powershell
.\routing-service\mvnw.cmd -B -ntp -f routing-service\pom.xml "-Dtest=RoutingServiceTest,RuleEvaluationServiceTest" test
```

### RAG Service

```powershell
.\rag-service\mvnw.cmd -B -ntp -f rag-service\pom.xml "-Dtest=RagServiceTest" test
```

## 3. Optional: Run All Tests for a Module

Example:

```powershell
.\ticket-service\mvnw.cmd -B -ntp -f ticket-service\pom.xml test
```

## What This Covers

- Controller layer tests for existing controllers (`ticket-service`, `ai-analysis-service`)
- Service layer tests for core business logic (`ticket`, `ai-analysis`, `routing`, `rag`)
- Event/outbox behavior and fallback paths

## Docker Troubleshooting Quick Checks

If Docker-based runs behave inconsistently, use:

```powershell
docker context use desktop-linux
docker version
docker ps
```

If Docker API returns `500` on `dockerDesktopLinuxEngine`, restart Docker Desktop and retry.
If app containers are up but Eureka is empty, verify DB/Kafka readiness and service startup logs first.
