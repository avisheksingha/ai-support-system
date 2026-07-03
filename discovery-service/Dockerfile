# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy parent POM
COPY aisupport-parent ./aisupport-parent

# Copy this service
COPY discovery-service ./discovery-service

# Install parent POM for clean module build
RUN mvn -N -f aisupport-parent/pom.xml clean install -DskipTests

# Build the service and copy the repackaged jar to a known location
WORKDIR /app/discovery-service
RUN mvn clean package -DskipTests \
  && JAR=$(ls target/*.jar | grep -v 'original' | grep -v 'sources' | head -n1) \
  && echo "Using jar: $JAR" \
  && cp "$JAR" /app/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/app.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","app.jar"]
