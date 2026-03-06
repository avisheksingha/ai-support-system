# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy parent and common library
COPY aisupport-parent ./aisupport-parent
COPY common-library ./common-library

# Copy this service
COPY ticket-service ./ticket-service

# 1) Build and install parent
RUN mvn -f aisupport-parent/pom.xml clean install -DskipTests

# 2) Build & install common-library into local repo so service can resolve it
RUN mvn -f common-library/pom.xml clean install -DskipTests

# Build the service and copy the repackaged jar to a known location
WORKDIR /app/ticket-service
RUN mvn clean package -DskipTests \
  && JAR=$(ls target/*.jar | grep -v 'original' | grep -v 'sources' | head -n1) \
  && echo "Using jar: $JAR" \
  && cp "$JAR" /app/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/ticket-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
