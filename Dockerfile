# Multi-stage build for AS400 JDBC Performance Tester
FROM maven:3.9.5-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY mvnw ./
COPY mvnw.bat ./
COPY .mvn ./.mvn

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/latency-test-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/health || exit 1

# Expose port
EXPOSE ${PORT:-8080}

# Environment variables with defaults
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    PORT=8080 \
    JDBC_POOL_SIZE=10 \
    JDBC_CONNECTION_TIMEOUT=30000 \
    JDBC_SOCKET_TIMEOUT=0 \
    JDBC_TCP_NO_DELAY=true \
    JDBC_AUTO_COMMIT=true \
    JDBC_BATCH_SIZE=100

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="AS400 Performance Team" \
      version="1.0" \
      description="AS400 JDBC Performance Testing Application" \
      java.version="21" \
      framework="Micronaut 4.8.3"