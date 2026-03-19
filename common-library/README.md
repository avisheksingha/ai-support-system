# Common Library

Shared module containing reusable components, DTOs, and utilities for the AI Support System microservices.

## Contents

- **DTOs**: Shared Data Transfer Objects (e.g., `AnalysisResultDTO`, `TicketDTO`).
- **Enums**: Centralized Enums (e.g., `TicketStatus`, `TicketPriority`).
- **Exceptions**: Common exception classes (e.g., `ResourceNotFoundException`, `OutboxEventException`) and global error handling structures.
- **Utilities / Constants**: Helper classes and strictly defined constants (e.g., `Correlation`, `HttpHeaders`, `KafkaTopics`, `KafkaGroups`).
- **Events**: Shared event models for Kafka-based communication (e.g., `TicketCreatedEvent`, `TicketAnalyzedEvent`, `TicketRoutedEvent`).

## Usage

Add this dependency to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.aisupport</groupId>
    <artifactId>common-library</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Building

To install this library into your local Maven repository so other services can find it:

```bash
mvn clean install
```
