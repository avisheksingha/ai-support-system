# Common Library

Shared module containing reusable components, DTOs, and utilities for the AI Support System microservices.

## Contents
- **DTOs**: Shared Data Transfer Objects (e.g., `AnalysisResultDTO`, `TicketDTO`, `RuleEvaluationRequest`)
- **Exceptions**: Common exception classes and global error handlers
- **Utilities**: Helper classes, constants, and shared configurations

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
