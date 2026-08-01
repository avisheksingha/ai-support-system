# Common Library

Shared module containing reusable components, DTOs, utilities, and common configuration for the AI Support System microservices.

## Contents

- **DTOs**: Shared Data Transfer Objects (e.g., `AnalysisResultDTO`, `TicketDTO`).
- **Enums**: Centralized Enums (e.g., `TicketStatus`, `TicketPriority`).
- **Exceptions**: Common exception classes (e.g., `ResourceNotFoundException`, `OutboxEventException`) and global error handling structures.
- **Utilities / Constants**: Helper classes and strictly defined constants (e.g., `Correlation`, `HttpHeaders`, `KafkaTopics`, `KafkaGroups`, `DateTimeUtil`). All temporal values use `java.time.Instant` — `DateTimeUtil` provides ISO/legacy formatting, parsing, and timezone-aware display helpers.
- **Events**: Shared event models for Kafka-based communication (e.g., `TicketCreatedEvent`, `TicketAnalyzedEvent`, `TicketRoutedEvent`).
- **Configuration**: Centralized Spring Boot properties shared across all consuming services.

## Shared Configuration

Common properties are distributed via `spring.config.import` and automatically inherited by all domain microservices that depend on this library.

| File                                  | Scope   | Contents                                       |
| ------------------------------------- | ------- | ---------------------------------------------- |
| `common-application.properties`       | All     | JPA defaults, Actuator info, OpenAPI/Swagger   |
| `common-application-local.properties` | `local` | Actuator endpoint exposure for local dev       |

Services import the base file via:

```properties
spring.config.import=classpath:common-application.properties
```

The `local` profile variant (`common-application-local.properties`) is loaded automatically by Spring Boot when the `local` profile is active.

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
