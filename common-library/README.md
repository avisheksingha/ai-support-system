# Common Library

Shared module containing reusable components for the AI Support System.

## Contents
- **DTOs**: Shared Data Transfer Objects (e.g., `AnalysisResultDTO`, `TicketDTO`)
- **Exceptions**: Common exception classes (e.g., `ResourceNotFoundException`)
- **Utilities**: Helper classes and constants

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
