package com.lkww.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@ConfigurationProperties("jdbc")
@Requires(property = "jdbc.enabled", value = "true", defaultValue = "true")
public record JdbcConfiguration(
    Boolean tcpNoDelay,
    Integer poolSize,
    Integer connectionTimeout,
    Integer socketTimeout,
    Integer batchSize,
    Boolean autoCommit,
    Integer fetchSize,
    Integer queryTimeout,
    Boolean threadUsed,
    String naming,
    String libraries,
    Boolean cursorHold,
    Boolean extendedDynamic,
    Integer blockSize,
    Boolean statementCache,
    String url,
    String username,
    String password,
    String driverClassName
) {
    
    public JdbcConfiguration {
        if (tcpNoDelay == null) {
            tcpNoDelay = Boolean.valueOf(System.getenv().getOrDefault("JDBC_TCP_NO_DELAY", "true"));
        }
        if (poolSize == null) {
            poolSize = Integer.valueOf(System.getenv().getOrDefault("JDBC_POOL_SIZE", "10"));
        }
        if (connectionTimeout == null) {
            connectionTimeout = Integer.valueOf(System.getenv().getOrDefault("JDBC_CONNECTION_TIMEOUT", "30000"));
        }
        if (socketTimeout == null) {
            socketTimeout = Integer.valueOf(System.getenv().getOrDefault("JDBC_SOCKET_TIMEOUT", "0"));
        }
        if (batchSize == null) {
            batchSize = Integer.valueOf(System.getenv().getOrDefault("JDBC_BATCH_SIZE", "100"));
        }
        if (autoCommit == null) {
            autoCommit = Boolean.valueOf(System.getenv().getOrDefault("JDBC_AUTO_COMMIT", "true"));
        }
        if (fetchSize == null) {
            fetchSize = Integer.valueOf(System.getenv().getOrDefault("JDBC_FETCH_SIZE", "1000"));
        }
        if (queryTimeout == null) {
            queryTimeout = Integer.valueOf(System.getenv().getOrDefault("JDBC_QUERY_TIMEOUT", "0"));
        }
        if (threadUsed == null) {
            threadUsed = Boolean.valueOf(System.getenv().getOrDefault("JDBC_THREAD_USED", "true"));
        }
        if (naming == null) {
            naming = System.getenv().getOrDefault("JDBC_NAMING", "sql");
        }
        if (libraries == null) {
            libraries = System.getenv().getOrDefault("JDBC_LIBRARIES", "");
        }
        if (cursorHold == null) {
            cursorHold = Boolean.valueOf(System.getenv().getOrDefault("JDBC_CURSOR_HOLD", "true"));
        }
        if (extendedDynamic == null) {
            extendedDynamic = Boolean.valueOf(System.getenv().getOrDefault("JDBC_EXTENDED_DYNAMIC", "true"));
        }
        if (blockSize == null) {
            blockSize = Integer.valueOf(System.getenv().getOrDefault("JDBC_BLOCK_SIZE", "512"));
        }
        if (statementCache == null) {
            statementCache = Boolean.valueOf(System.getenv().getOrDefault("JDBC_STATEMENT_CACHE", "true"));
        }
        if (url == null) {
            url = System.getenv().getOrDefault("JDBC_URL", "jdbc:as400://localhost");
        }
        if (username == null) {
            username = System.getenv().getOrDefault("JDBC_USERNAME", "user");
        }
        if (password == null) {
            password = System.getenv().getOrDefault("JDBC_PASSWORD", "password");
        }
        if (driverClassName == null) {
            driverClassName = System.getenv().getOrDefault("JDBC_DRIVER_CLASS", "com.ibm.as400.access.AS400JDBCDriver");
        }
    }
}