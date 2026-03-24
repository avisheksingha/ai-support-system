# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy parent and common library
COPY aisupport-parent ./aisupport-parent
COPY common-library ./common-library

# Copy only this service
COPY ai-analysis-service ./ai-analysis-service

# 1) Build and install parent if it produces an installable artifact (optional)
#   If aisupport-parent is a pom packaging parent only, the next step (install common) will pick up relativePath.
#   If aisupport-parent must be installed, run: mvn -f aisupport-parent/pom.xml clean install -DskipTests
RUN mvn -f aisupport-parent/pom.xml clean install -DskipTests

# 2) Build & install common-library into local repo so service can resolve it
RUN mvn -f common-library/pom.xml clean install -DskipTests

# Build the service and copy the repackaged jar to a known location
WORKDIR /app/ai-analysis-service
RUN mvn clean package -DskipTests \
  && JAR=$(ls target/*.jar | grep -v 'original' | grep -v 'sources' | head -n1) \
  && echo "Using jar: $JAR" \
  && cp "$JAR" /app/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the jar from the builder stage
COPY --from=builder /app/app.jar app.jar

# Set default profiles
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar"]
