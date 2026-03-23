# Testing Guide

This guide provides the fastest way to run the most relevant tests for this project.

## Prerequisites

- Java 21
- Maven Wrapper (`mvnw.cmd`) is already included in each module

## Important Local Note (Windows)

If your environment has an invalid `MAVEN_OPTS` value (for example just a path like `F:\packages\.m2`), Maven can fail to start.

Use this before running commands in the same PowerShell session:

```powershell
$env:MAVEN_OPTS=''
```

## 1. Install Parent and Shared Library

From repo root:

```powershell
$env:MAVEN_OPTS=''; .\aisupport-parent\mvnw.cmd -B -ntp -f aisupport-parent\pom.xml -N install
$env:MAVEN_OPTS=''; .\common-library\mvnw.cmd -B -ntp -f common-library\pom.xml clean install -DskipTests
```

## 2. Run Core Controller/Service Test Pack

### Ticket Service

```powershell
$env:MAVEN_OPTS=''; .\ticket-service\mvnw.cmd -B -ntp -f ticket-service\pom.xml "-Dtest=TicketControllerTest,TicketServiceBehaviorTest,GlobalExceptionHandlerTest,OutboxEventPublisherTest" test
```

### AI Analysis Service

```powershell
$env:MAVEN_OPTS=''; .\ai-analysis-service\mvnw.cmd -B -ntp -f ai-analysis-service\pom.xml "-Dtest=AnalysisControllerTest,AnalysisProcessingServiceTest,AnalysisQueryServiceTest" test
```

### Routing Service

```powershell
$env:MAVEN_OPTS=''; .\routing-service\mvnw.cmd -B -ntp -f routing-service\pom.xml "-Dtest=RoutingServiceTest,RuleEvaluationServiceTest" test
```

### RAG Service

```powershell
$env:MAVEN_OPTS=''; .\rag-service\mvnw.cmd -B -ntp -f rag-service\pom.xml "-Dtest=RagServiceTest" test
```

## 3. Optional: Run All Tests for a Module

Example:

```powershell
$env:MAVEN_OPTS=''; .\ticket-service\mvnw.cmd -B -ntp -f ticket-service\pom.xml test
```

## What This Covers

- Controller layer tests for existing controllers (`ticket-service`, `ai-analysis-service`)
- Service layer tests for core business logic (`ticket`, `ai-analysis`, `routing`, `rag`)
- Event/outbox behavior and fallback paths
