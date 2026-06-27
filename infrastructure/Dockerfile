# Multi-stage build for Cloud Backup System

# Stage 1: Build
FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /build

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn -B -DskipTests clean package

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from builder stage
COPY --from=builder /build/target/cloud-backup-system-0.0.1-SNAPSHOT.jar app.jar

# Create a non-root user for security
RUN useradd -m -u 1000 appuser && chown appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
