# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only this service (no parent/common needed)
COPY api-gateway ./api-gateway

# Build this service
WORKDIR /app/api-gateway
RUN mvn clean package -DskipTests \
    && JAR=$(ls target/*.jar | grep -v 'original' | grep -v 'sources' | head -n1) \
    && cp "$JAR" /app/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/app.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
