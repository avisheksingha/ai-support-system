# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy parent and common library
COPY aisupport-parent ./aisupport-parent
COPY common-library ./common-library

# Copy this service
COPY rag-service ./rag-service

# 1) Install parent POM
RUN mvn -f aisupport-parent/pom.xml clean install -DskipTests

# 2) Install common-library for module dependency resolution
RUN mvn -f common-library/pom.xml clean install -DskipTests

# Build service and copy jar to known location
WORKDIR /app/rag-service
RUN mvn clean package -DskipTests \
  && JAR=$(ls target/*.jar | grep -v 'original' | grep -v 'sources' | head -n1) \
  && echo "Using jar: $JAR" \
  && cp "$JAR" /app/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8085

ENTRYPOINT ["java","-jar","app.jar"]
