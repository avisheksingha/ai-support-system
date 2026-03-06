# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy this service
COPY discovery-service ./discovery-service

# Build the service and copy the repackaged jar to a known location
WORKDIR /app/discovery-service
RUN mvn clean package -DskipTests \
  && JAR=$(ls target/*.jar | grep -v 'original' | grep -v 'sources' | head -n1) \
  && echo "Using jar: $JAR" \
  && cp "$JAR" /app/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/discovery-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
