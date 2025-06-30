# AS400 JDBC Performance Tester

A specialized Java application designed to test JDBC performance parameters when connecting to AS400 servers in high-latency environments. Built with Micronaut 4.8.3 and Java 21.

## Overview

This application serves as a benchmarking tool for database administrators and developers working with IBM AS400 systems. It provides systematic testing of various JDBC configuration parameters to optimize database connection performance in high-latency network environments.

## Features

- **SELECT Statement Throughput Testing**: Optimized for testing SELECT query performance on AS400 connections
- **Environment-Based Configuration**: All JDBC parameters configurable via environment variables
- **Real-time Metrics**: QPS, response times, connection pool metrics, and error tracking
- **REST API**: Full API for health checks, metrics retrieval, and test management
- **Containerized**: Docker-ready with security best practices
- **Prometheus Integration**: Metrics export for monitoring and alerting

## Quick Start

### Prerequisites

- Java 21 LTS
- Maven 3.9+
- Access to an AS400 server
- Docker (optional, for containerized deployment)

### Running Locally

1. **Clone and build the application:**
   ```bash
   git clone <repository-url>
   cd latency-performance-test
   ./mvnw clean compile
   ```

2. **Set environment variables:**
   ```bash
   export JDBC_URL="jdbc:as400://your-as400-server:port/database"
   export JDBC_USERNAME="your-username"
   export JDBC_PASSWORD="your-password"
   export JDBC_POOL_SIZE=20
   export JDBC_TCP_NO_DELAY=true
   ```

3. **Run the application:**
   ```bash
   ./mvnw mn:run
   ```

4. **Access the application:**
   - Health Check: http://localhost:8080/health
   - Metrics: http://localhost:8080/metrics
   - Configuration: http://localhost:8080/config
   - Prometheus Metrics: http://localhost:8080/prometheus

### Running with Docker

1. **Build the Docker image:**
   ```bash
   docker build -t as400-jdbc-tester .
   ```

2. **Run the container:**
   ```bash
   docker run -d \
     -p 8080:8080 \
     -e JDBC_URL="jdbc:as400://your-as400-server:port/database" \
     -e JDBC_USERNAME="your-username" \
     -e JDBC_PASSWORD="your-password" \
     -e JDBC_POOL_SIZE=20 \
     --name as400-tester \
     as400-jdbc-tester
   ```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JDBC_URL` | `jdbc:as400://localhost` | AS400 JDBC connection URL |
| `JDBC_USERNAME` | `user` | Database username |
| `JDBC_PASSWORD` | `password` | Database password |
| `JDBC_DRIVER_CLASS` | `com.ibm.as400.access.AS400JDBCDriver` | JDBC driver class |
| `JDBC_POOL_SIZE` | `10` | Maximum connection pool size |
| `JDBC_CONNECTION_TIMEOUT` | `30000` | Connection timeout in milliseconds |
| `JDBC_SOCKET_TIMEOUT` | `0` | Socket timeout in milliseconds |
| `JDBC_TCP_NO_DELAY` | `true` | Enable TCP no delay |
| `JDBC_AUTO_COMMIT` | `true` | Enable auto-commit |
| `JDBC_BATCH_SIZE` | `100` | Batch size for operations |
| `JDBC_FETCH_SIZE` | `1000` | Number of rows to fetch per round trip |
| `JDBC_QUERY_TIMEOUT` | `0` | Query timeout in seconds (0 = no timeout) |
| `JDBC_THREAD_USED` | `true` | Use threaded server mode (better performance) |
| `JDBC_NAMING` | `sql` | Naming convention: 'sql' or 'system' |
| `JDBC_LIBRARIES` | `` | Default library list (comma-separated) |
| `JDBC_CURSOR_HOLD` | `true` | Hold cursors across commits |
| `JDBC_EXTENDED_DYNAMIC` | `true` | Use extended dynamic SQL (better performance) |
| `JDBC_BLOCK_SIZE` | `512` | Block size for record blocking (KB) |
| `JDBC_STATEMENT_CACHE` | `true` | Enable statement/package caching |
| `PORT` | `8080` | Application port |

### Application Properties

The application supports both properties and YAML configuration files. Key settings are in:
- `src/main/resources/application.properties` - Database and connection settings
- `src/main/resources/application.yml` - Logging and metrics configuration

## API Endpoints

### Health Check
```bash
GET /health
```
Returns application and database health status.

### Metrics
```bash
GET /metrics
```
Returns current performance metrics including QPS, response times, and connection statistics.

### Configuration

**Get Current Configuration:**
```bash
GET /config
```
Returns current JDBC configuration settings.

**Update Multiple Configuration Fields:**
```bash
PUT /config
Content-Type: application/json

{
  "fetchSize": 2000,
  "queryTimeout": 30,
  "poolSize": 20
}
```

**Update Single Configuration Field:**
```bash
PUT /config/fetchSize
Content-Type: text/plain

2000
```

**Available Configuration Fields (case-insensitive):**
- `tcpNoDelay`, `poolSize`, `connectionTimeout`, `socketTimeout`
- `batchSize`, `autoCommit`, `fetchSize`, `queryTimeout` 
- `threadUsed`, `naming`, `libraries`, `cursorHold`
- `extendedDynamic`, `blockSize`, `statementCache`
- `url`, `username`, `password`, `driverClassName`

**Note:** Configuration changes take effect immediately for new connections and queries. All changes are logged with the complete current configuration.

### Test Management

**Start Performance Test:**
```bash
POST /test/start
Content-Type: application/json

{
  "queryType": "SELECT",
  "duration": 60,
  "concurrency": 10,
  "query": "SELECT COUNT(*) FROM your_table"
}
```

**Stop Running Test:**
```bash
POST /test/stop
```

**Get Test Results:**
```bash
GET /test/results
```

## Performance Testing

### Example Test Scenarios

1. **Basic Connectivity Test:**
   ```bash
   curl -X POST http://localhost:8080/test/start \
     -H "Content-Type: application/json" \
     -d '{"duration":30,"concurrency":1,"query":"SELECT 1 FROM SYSIBM.SYSDUMMY1"}'
   ```

2. **Large Table Test (Memory Efficient):**
   ```bash
   curl -X POST http://localhost:8080/test/start \
     -H "Content-Type: application/json" \
     -d '{"duration":120,"concurrency":10,"query":"SELECT * FROM QSYS2.SYSTABLES"}'
   ```

3. **High Concurrency Test:**
   ```bash
   curl -X POST http://localhost:8080/test/start \
     -H "Content-Type: application/json" \
     -d '{"duration":120,"concurrency":50,"query":"SELECT COUNT(*) FROM your_large_table"}'
   ```

4. **Fetch Size Optimization Test:**
   ```bash
   # Test with small fetch size (more round trips)
   curl -X POST http://localhost:8080/test/start \
     -H "Content-Type: application/json" \
     -d '{"duration":60,"concurrency":5,"query":"SELECT * FROM your_large_table FETCH FIRST 50000 ROWS ONLY"}' \
     --env JDBC_FETCH_SIZE=100
   
   # Test with large fetch size (fewer round trips, more memory)
   curl -X POST http://localhost:8080/test/start \
     -H "Content-Type: application/json" \
     -d '{"duration":60,"concurrency":5,"query":"SELECT * FROM your_large_table FETCH FIRST 50000 ROWS ONLY"}' \
     --env JDBC_FETCH_SIZE=5000
   ```

5. **Network Latency Impact Analysis:**
   ```bash
   # Test with TCP_NO_DELAY=false
   docker run -e JDBC_TCP_NO_DELAY=false -e JDBC_POOL_SIZE=5 as400-jdbc-tester
   
   # Test with TCP_NO_DELAY=true
   docker run -e JDBC_TCP_NO_DELAY=true -e JDBC_POOL_SIZE=5 as400-jdbc-tester
   ```

## Monitoring and Observability

### Prometheus Metrics

The application exposes Prometheus-compatible metrics at `/prometheus`:

- `jdbc_query_duration` - Query execution time histogram
- `jdbc_query_total` - Total number of queries executed
- `jdbc_error_total` - Total number of query errors
- `jdbc_connections_active` - Number of active connections
- `jdbc_connections_idle` - Number of idle connections
- `jdbc_qps` - Current queries per second

### Logging

Structured JSON logging is configured with the following loggers:

- `com.lkww` - Application logs (DEBUG level)
- `com.ibm.as400` - AS400 driver logs (INFO level)
- `com.zaxxer.hikari` - Connection pool logs (DEBUG level)

## Development

### Building from Source

```bash
git clone <repository-url>
cd latency-performance-test
./mvnw clean compile
```

### Running Tests

```bash
./mvnw test
```

### Building Docker Image

```bash
./mvnw clean package
docker build -t as400-jdbc-tester .
```

## Deployment

### Helm Chart (Recommended)

The application includes a production-ready Helm chart for easy Kubernetes deployment.

#### Quick Start with Helm

1. **Install the Helm chart:**
   ```bash
   helm install as400-tester ./helm/as400-jdbc-tester \
     --set jdbc.url="jdbc:as400://your-as400-server:port/database" \
     --set jdbc.username="your-username" \
     --set jdbc.password="your-password"
   ```

2. **Access the application:**
   ```bash
   kubectl port-forward service/as400-tester-as400-jdbc-tester 8080:8080
   ```

#### Production Deployment

Create a custom values file for production:

```yaml
# production-values.yaml
replicaCount: 3

image:
  repository: your-registry/as400-jdbc-tester
  tag: "1.0.0"
  pullPolicy: IfNotPresent

imagePullSecrets:
  - name: registry-secret

# JDBC Configuration
jdbc:
  url: "jdbc:as400://production-as400:port/database"
  username: "prod-user"
  password: "secure-password"
  poolSize: 20
  fetchSize: 5000
  tcpNoDelay: true
  threadUsed: true

resources:
  limits:
    cpu: 2000m
    memory: 2Gi
  requests:
    cpu: 500m
    memory: 1Gi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70

ingress:
  enabled: true
  className: "nginx"
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: as400-tester.company.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: as400-tester-tls
      hosts:
        - as400-tester.company.com

monitoring:
  prometheus:
    enabled: true

podDisruptionBudget:
  enabled: true
  minAvailable: 2
```

Deploy with the production configuration:
```bash
helm install as400-tester ./helm/as400-jdbc-tester -f production-values.yaml
```

#### Helm Chart Configuration

**Key Configuration Options:**

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `image.repository` | Container image repository | `as400-jdbc-tester` |
| `image.tag` | Container image tag | `latest` |
| `jdbc.url` | AS400 JDBC URL | `jdbc:as400://localhost` |
| `jdbc.username` | Database username | `user` |
| `jdbc.password` | Database password | `password` |
| `jdbc.poolSize` | Connection pool size | `10` |
| `jdbc.fetchSize` | JDBC fetch size | `1000` |
| `resources.limits.memory` | Memory limit | `1Gi` |
| `resources.limits.cpu` | CPU limit | `1000m` |
| `autoscaling.enabled` | Enable HPA | `false` |
| `ingress.enabled` | Enable ingress | `false` |
| `monitoring.prometheus.enabled` | Enable Prometheus metrics | `true` |

**Security Features:**
- Non-root container execution
- Read-only root filesystem
- Security contexts applied
- Secrets for sensitive data
- Service account with minimal permissions

#### Helm Commands

```bash
# Install
helm install my-release ./helm/as400-jdbc-tester

# Upgrade
helm upgrade my-release ./helm/as400-jdbc-tester -f values.yaml

# Uninstall
helm uninstall my-release

# Check status
helm status my-release

# Get values
helm get values my-release
```

### Manual Kubernetes Deployment

For environments without Helm, you can use the raw Kubernetes manifests:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: as400-jdbc-tester
spec:
  replicas: 3
  selector:
    matchLabels:
      app: as400-jdbc-tester
  template:
    metadata:
      labels:
        app: as400-jdbc-tester
    spec:
      containers:
      - name: as400-jdbc-tester
        image: as400-jdbc-tester:latest
        ports:
        - containerPort: 8080
        env:
        - name: JDBC_URL
          value: "jdbc:as400://as400-server:port/database"
        - name: JDBC_USERNAME
          valueFrom:
            secretKeyRef:
              name: as400-credentials
              key: username
        - name: JDBC_PASSWORD
          valueFrom:
            secretKeyRef:
              name: as400-credentials
              key: password
        resources:
          limits:
            memory: 1Gi
            cpu: 1000m
          requests:
            memory: 512Mi
            cpu: 250m
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

## Troubleshooting

### Common Issues

1. **Connection Timeout Errors:**
   - Increase `JDBC_CONNECTION_TIMEOUT`
   - Check network connectivity to AS400 server
   - Verify AS400 server is accepting connections

2. **High Latency Performance:**
   - Enable `JDBC_TCP_NO_DELAY=true`
   - Adjust `JDBC_SOCKET_TIMEOUT`
   - Optimize `JDBC_POOL_SIZE` for your workload
   - **Tune `JDBC_FETCH_SIZE`**: Critical for high-latency environments
     - Higher values (e.g., 5000-10000) reduce round trips but increase memory usage
     - Lower values (e.g., 100-500) reduce memory but increase latency impact
     - Test different values to find optimal balance for your network conditions

3. **AS400-Specific Performance Tuning:**
   - **`JDBC_THREAD_USED=true`**: Essential for performance - uses threaded server jobs
   - **`JDBC_EXTENDED_DYNAMIC=true`**: Improves SQL performance with package caching
   - **`JDBC_BLOCK_SIZE`**: Increase to 1024-2048 for better record blocking in high-latency networks
   - **`JDBC_NAMING=sql`**: Use SQL naming for better performance vs system naming
   - **`JDBC_STATEMENT_CACHE=true`**: Reduces statement preparation overhead
   - **`JDBC_CURSOR_HOLD=true`**: Maintains cursors across commits for better throughput

4. **Memory Issues:**
   - Reduce `JDBC_POOL_SIZE`
   - Reduce `JDBC_FETCH_SIZE` to lower memory per connection
   - Reduce `JDBC_BLOCK_SIZE` if memory-constrained
   - Set appropriate JVM heap size: `-Xmx512m`
   - Monitor connection pool metrics

### Debug Mode

Enable debug logging:
```bash
export MICRONAUT_LOGGER_LEVELS_COM_LKWW=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review application logs for error details

---

## Technical Documentation

- [Micronaut 4.8.3 Documentation](https://docs.micronaut.io/4.8.3/guide/index.html)
- [JT400 JDBC Driver Documentation](https://www.ibm.com/docs/en/i/7.5?topic=toolbox-java-jdbc-driver)
- [HikariCP Connection Pool](https://github.com/brettwooldridge/HikariCP)
- [Micrometer Metrics](https://micrometer.io/docs)