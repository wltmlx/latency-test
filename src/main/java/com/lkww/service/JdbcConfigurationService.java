package com.lkww.service;

import com.lkww.config.JdbcConfiguration;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class JdbcConfigurationService {
    
    private static final Logger LOG = LoggerFactory.getLogger(JdbcConfigurationService.class);
    
    private final AtomicReference<JdbcConfiguration> currentConfiguration;
    
    public JdbcConfigurationService(JdbcConfiguration initialConfiguration) {
        this.currentConfiguration = new AtomicReference<>(initialConfiguration);
        logCurrentConfiguration("Initial configuration loaded");
    }
    
    public JdbcConfiguration getCurrentConfiguration() {
        return currentConfiguration.get();
    }
    
    public JdbcConfiguration updateConfiguration(JdbcConfigurationUpdateRequest request) {
        JdbcConfiguration current = currentConfiguration.get();
        
        JdbcConfiguration updated = new JdbcConfiguration(
            request.tcpNoDelay() != null ? request.tcpNoDelay() : current.tcpNoDelay(),
            request.poolSize() != null ? request.poolSize() : current.poolSize(),
            request.connectionTimeout() != null ? request.connectionTimeout() : current.connectionTimeout(),
            request.socketTimeout() != null ? request.socketTimeout() : current.socketTimeout(),
            request.batchSize() != null ? request.batchSize() : current.batchSize(),
            request.autoCommit() != null ? request.autoCommit() : current.autoCommit(),
            request.fetchSize() != null ? request.fetchSize() : current.fetchSize(),
            request.queryTimeout() != null ? request.queryTimeout() : current.queryTimeout(),
            request.threadUsed() != null ? request.threadUsed() : current.threadUsed(),
            request.naming() != null ? request.naming() : current.naming(),
            request.libraries() != null ? request.libraries() : current.libraries(),
            request.cursorHold() != null ? request.cursorHold() : current.cursorHold(),
            request.extendedDynamic() != null ? request.extendedDynamic() : current.extendedDynamic(),
            request.blockSize() != null ? request.blockSize() : current.blockSize(),
            request.statementCache() != null ? request.statementCache() : current.statementCache(),
            request.url() != null ? request.url() : current.url(),
            request.username() != null ? request.username() : current.username(),
            request.password() != null ? request.password() : current.password(),
            request.driverClassName() != null ? request.driverClassName() : current.driverClassName()
        );
        
        currentConfiguration.set(updated);
        logCurrentConfiguration("Configuration updated successfully");
        
        return updated;
    }
    
    public JdbcConfiguration updateSingleField(String fieldName, String value) {
        JdbcConfiguration current = currentConfiguration.get();
        JdbcConfiguration updated;
        
        switch (fieldName.toLowerCase()) {
            case "tcpnodelay" -> updated = new JdbcConfiguration(Boolean.parseBoolean(value), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "poolsize" -> updated = new JdbcConfiguration(current.tcpNoDelay(), Integer.parseInt(value), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "connectiontimeout" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), Integer.parseInt(value), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "sockettimeout" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), Integer.parseInt(value), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "batchsize" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), Integer.parseInt(value), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "autocommit" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), Boolean.parseBoolean(value), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "fetchsize" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), Integer.parseInt(value), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "querytimeout" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), Integer.parseInt(value), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "threadused" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), Boolean.parseBoolean(value), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "naming" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), value, current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "libraries" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), value, current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "cursorhold" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), Boolean.parseBoolean(value), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "extendeddynamic" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), Boolean.parseBoolean(value), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "blocksize" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), Integer.parseInt(value), current.statementCache(), current.url(), current.username(), current.password(), current.driverClassName());
            case "statementcache" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), Boolean.parseBoolean(value), current.url(), current.username(), current.password(), current.driverClassName());
            case "url" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), value, current.username(), current.password(), current.driverClassName());
            case "username" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), value, current.password(), current.driverClassName());
            case "password" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), value, current.driverClassName());
            case "driverclassname" -> updated = new JdbcConfiguration(current.tcpNoDelay(), current.poolSize(), current.connectionTimeout(), current.socketTimeout(), current.batchSize(), current.autoCommit(), current.fetchSize(), current.queryTimeout(), current.threadUsed(), current.naming(), current.libraries(), current.cursorHold(), current.extendedDynamic(), current.blockSize(), current.statementCache(), current.url(), current.username(), current.password(), value);
            default -> throw new IllegalArgumentException("Unknown configuration field: " + fieldName);
        }
        
        currentConfiguration.set(updated);
        logCurrentConfiguration("Single field '" + fieldName + "' updated to '" + value + "'");
        
        return updated;
    }
    
    private void logCurrentConfiguration(String message) {
        JdbcConfiguration config = currentConfiguration.get();
        
        LOG.info("{}: tcpNoDelay={}, poolSize={}, connectionTimeout={}, socketTimeout={}, " +
                "batchSize={}, autoCommit={}, fetchSize={}, queryTimeout={}, threadUsed={}, " +
                "naming='{}', libraries='{}', cursorHold={}, extendedDynamic={}, blockSize={}, " +
                "statementCache={}, url='{}', username='{}', driverClassName='{}'",
                message,
                config.tcpNoDelay(), config.poolSize(), config.connectionTimeout(), config.socketTimeout(),
                config.batchSize(), config.autoCommit(), config.fetchSize(), config.queryTimeout(), config.threadUsed(),
                config.naming(), config.libraries(), config.cursorHold(), config.extendedDynamic(), config.blockSize(),
                config.statementCache(), config.url(), config.username(), config.driverClassName());
    }
    
    public record JdbcConfigurationUpdateRequest(
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
    ) {}
}