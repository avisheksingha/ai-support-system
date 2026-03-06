# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only this service (no parent/common needed)
COPY . ./api-gateway

# Build this service
RUN mvn -pl api-gateway clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]

