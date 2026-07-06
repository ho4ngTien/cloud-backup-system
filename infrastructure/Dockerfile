# =============================================================
#  CloudSafe – Multi-stage Docker Build
#  Stage 1: Build the fat JAR with Maven
#  Stage 2: Minimal JRE runtime image
# =============================================================

# -------- Stage 1: Build --------
FROM maven:3.9.16-eclipse-temurin-25 AS builder

WORKDIR /build

# Cache Maven dependencies first
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:go-offline -f pom.xml || true

# Copy source and build
COPY src ./src
RUN mvn -B package -DskipTests -f pom.xml

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:25-jre-alpine AS runtime

LABEL maintainer="CloudSafe Team <support@cloudsafe.io>"
LABEL version="1.0.0"
LABEL description="CloudSafe Cloud Backup & Disaster Recovery"

# Security: run as non-root user
RUN addgroup -S cloudsafe && adduser -S cloudsafe -G cloudsafe

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Create log directory
RUN mkdir -p /app/logs && chown cloudsafe:cloudsafe /app/logs

USER cloudsafe

EXPOSE 8080

# JVM tuning: container-aware GC, UTF-8, timezone
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Dfile.encoding=UTF-8", \
    "-Duser.timezone=Asia/Ho_Chi_Minh", \
    "-jar", "app.jar"]
