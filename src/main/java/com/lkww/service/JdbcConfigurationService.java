package com.lkww.service;

import com.lkww.config.JdbcConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Singleton;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@Singleton
public class JdbcConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcConfigurationService.class);
    @Getter
    private final JdbcConfiguration currentConfiguration;
    private final DataSource dataSource;

    public JdbcConfigurationService(JdbcConfiguration initialConfiguration, DataSource dataSource) {
        // Create a mutable copy for runtime updates
        this.currentConfiguration = new JdbcConfiguration();
        this.dataSource = dataSource;
        copyConfiguration(initialConfiguration, this.currentConfiguration);
        logCurrentConfiguration("Initial configuration loaded");
    }

    public JdbcConfiguration updateConfiguration(JdbcConfigurationUpdateRequest request) {
        synchronized (currentConfiguration) {
            boolean needsConnectionRefresh = false;

            // HikariCP settings - using Lombok-generated setters
            if (request.maximumPoolSize() != null) {
                currentConfiguration.setMaximumPoolSize(request.maximumPoolSize());
                applyPoolSizeChange();
            }
            if (request.minimumIdle() != null) {
                currentConfiguration.setMinimumIdle(request.minimumIdle());
                applyPoolSizeChange();
            }
            if (request.connectionTimeout() != null) {
                currentConfiguration.setConnectionTimeout(request.connectionTimeout());
                applyPoolTimeoutChanges();
            }
            if (request.idleTimeout() != null) {
                currentConfiguration.setIdleTimeout(request.idleTimeout());
                applyPoolTimeoutChanges();
            }
            if (request.maxLifetime() != null) {
                currentConfiguration.setMaxLifetime(request.maxLifetime());
                applyPoolTimeoutChanges();
            }
            if (request.leakDetectionThreshold() != null) {
                currentConfiguration.setLeakDetectionThreshold(request.leakDetectionThreshold());
                applyPoolTimeoutChanges();
            }
            if (request.autoCommit() != null) {
                currentConfiguration.setAutoCommit(request.autoCommit());
                needsConnectionRefresh = true;
            }

            // AS/400 driver properties - these require new connections
            if (request.naming() != null) {
                currentConfiguration.setNaming(request.naming());
                needsConnectionRefresh = true;
            }
            if (request.libraries() != null) {
                currentConfiguration.setLibraries(request.libraries());
                needsConnectionRefresh = true;
            }
            if (request.threadUsed() != null) {
                currentConfiguration.setThreadUsed(request.threadUsed());
                needsConnectionRefresh = true;
            }
            if (request.cursorHold() != null) {
                currentConfiguration.setCursorHold(request.cursorHold());
                needsConnectionRefresh = true;
            }
            if (request.extendedDynamic() != null) {
                currentConfiguration.setExtendedDynamic(request.extendedDynamic());
                needsConnectionRefresh = true;
            }
            if (request.packageCache() != null) {
                currentConfiguration.setPackageCache(request.packageCache());
                needsConnectionRefresh = true;
            }
            if (request.blockSize() != null) {
                currentConfiguration.setBlockSize(request.blockSize());
                needsConnectionRefresh = true;
            }
            if (request.socketTimeout() != null) {
                currentConfiguration.setSocketTimeout(request.socketTimeout());
                needsConnectionRefresh = true;
            }
            if (request.socketKeepAlive() != null) {
                currentConfiguration.setSocketKeepAlive(request.socketKeepAlive());
                needsConnectionRefresh = true;
            }
            if (request.tcpNoDelay() != null) {
                currentConfiguration.setTcpNoDelay(request.tcpNoDelay());
                needsConnectionRefresh = true;
            }

            // NEW: Critical AS/400 performance properties
            if (request.prefetch() != null) {
                currentConfiguration.setPrefetch(request.prefetch());
                needsConnectionRefresh = true;
            }
            if (request.queryOptimizeGoal() != null) {
                currentConfiguration.setQueryOptimizeGoal(request.queryOptimizeGoal());
                needsConnectionRefresh = true;
            }
            if (request.lobThreshold() != null) {
                currentConfiguration.setLobThreshold(request.lobThreshold());
                needsConnectionRefresh = true;
            }

            // Query performance settings
            if (request.queryTimeout() != null) {
                currentConfiguration.setQueryTimeout(request.queryTimeout());
            }
            if (request.fetchSize() != null) {
                currentConfiguration.setFetchSize(request.fetchSize());
            }

            // Apply connection refresh if needed
            if (needsConnectionRefresh) {
                refreshConnectionPool();
            }
        }

        logCurrentConfiguration("Configuration updated successfully");
        return currentConfiguration;
    }

    public JdbcConfiguration updateSingleField(String fieldName, String value) {
        synchronized (currentConfiguration) {
            boolean needsConnectionRefresh = false;

            switch (fieldName.toLowerCase()) {
                // HikariCP pool settings - using Lombok-generated setters
                case "maximumpoolsize" -> {
                    currentConfiguration.setMaximumPoolSize(Integer.parseInt(value));
                    applyPoolSizeChange();
                }
                case "minimumidle" -> {
                    currentConfiguration.setMinimumIdle(Integer.parseInt(value));
                    applyPoolSizeChange();
                }
                case "connectiontimeout" -> {
                    currentConfiguration.setConnectionTimeout(Integer.parseInt(value));
                    applyPoolTimeoutChanges();
                }
                case "idletimeout" -> {
                    currentConfiguration.setIdleTimeout(Integer.parseInt(value));
                    applyPoolTimeoutChanges();
                }
                case "maxlifetime" -> {
                    currentConfiguration.setMaxLifetime(Integer.parseInt(value));
                    applyPoolTimeoutChanges();
                }
                case "leakdetectionthreshold" -> {
                    currentConfiguration.setLeakDetectionThreshold(Integer.parseInt(value));
                    applyPoolTimeoutChanges();
                }
                case "autocommit" -> {
                    currentConfiguration.setAutoCommit(Boolean.parseBoolean(value));
                    needsConnectionRefresh = true;
                }

                // AS/400 driver properties - these require new connections
                case "naming" -> {
                    currentConfiguration.setNaming(value);
                    needsConnectionRefresh = true;
                }
                case "libraries" -> {
                    currentConfiguration.setLibraries(value);
                    needsConnectionRefresh = true;
                }
                case "threadused" -> {
                    currentConfiguration.setThreadUsed(Boolean.parseBoolean(value));
                    needsConnectionRefresh = true;
                }
                case "cursorhold" -> {
                    currentConfiguration.setCursorHold(Boolean.parseBoolean(value));
                    needsConnectionRefresh = true;
                }
                case "extendeddynamic" -> {
                    currentConfiguration.setExtendedDynamic(Boolean.parseBoolean(value));
                    needsConnectionRefresh = true;
                }
                case "packagecache" -> {
                    currentConfiguration.setPackageCache(Boolean.parseBoolean(value));
                    needsConnectionRefresh = true;
                }
                case "blocksize" -> {
                    currentConfiguration.setBlockSize(Integer.parseInt(value));
                    needsConnectionRefresh = true;
                }
                case "sockettimeout" -> {
                    currentConfiguration.setSocketTimeout(Integer.parseInt(value));
                    needsConnectionRefresh = true;
                }
                case "socketkeepalive" -> {
                    currentConfiguration.setSocketKeepAlive(Boolean.parseBoolean(value));
                    needsConnectionRefresh = true;
                }
                case "tcpnodelay" -> {
                    currentConfiguration.setTcpNoDelay(Boolean.parseBoolean(value));
                    needsConnectionRefresh = true;
                }

                // NEW: Critical AS/400 performance properties
                case "prefetch" -> {
                    currentConfiguration.setPrefetch(Boolean.parseBoolean(value));
                    needsConnectionRefresh = true;
                }
                case "queryoptimizegoal" -> {
                    currentConfiguration.setQueryOptimizeGoal(Integer.parseInt(value));
                    needsConnectionRefresh = true;
                }
                case "lobthreshold" -> {
                    currentConfiguration.setLobThreshold(Integer.parseInt(value));
                    needsConnectionRefresh = true;
                }

                // Query performance settings
                case "querytimeout" -> currentConfiguration.setQueryTimeout(Integer.parseInt(value));
                case "fetchsize" -> currentConfiguration.setFetchSize(Integer.parseInt(value));

                default -> throw new IllegalArgumentException("Unknown configuration field: " + fieldName);
            }

            // Apply connection refresh if needed
            if (needsConnectionRefresh) {
                refreshConnectionPool();
            }
        }

        logCurrentConfiguration("Single field '" + fieldName + "' updated to '" + value + "'");
        return currentConfiguration;
    }

    private void copyConfiguration(JdbcConfiguration source, JdbcConfiguration target) {
        // Using Lombok-generated getters and setters
        target.setMaximumPoolSize(source.getMaximumPoolSize());
        target.setMinimumIdle(source.getMinimumIdle());
        target.setConnectionTimeout(source.getConnectionTimeout());
        target.setIdleTimeout(source.getIdleTimeout());
        target.setMaxLifetime(source.getMaxLifetime());
        target.setLeakDetectionThreshold(source.getLeakDetectionThreshold());
        target.setAutoCommit(source.getAutoCommit());

        target.setNaming(source.getNaming());
        target.setLibraries(source.getLibraries());
        target.setThreadUsed(source.getThreadUsed());
        target.setCursorHold(source.getCursorHold());
        target.setExtendedDynamic(source.getExtendedDynamic());
        target.setPackageCache(source.getPackageCache());
        target.setBlockSize(source.getBlockSize());
        target.setSocketTimeout(source.getSocketTimeout());
        target.setSocketKeepAlive(source.getSocketKeepAlive());
        target.setTcpNoDelay(source.getTcpNoDelay());

        // Set defaults for new properties if source doesn't have them
        target.setPrefetch(source.getPrefetch() != null ? source.getPrefetch() : true);
        target.setQueryOptimizeGoal(source.getQueryOptimizeGoal() != null ? source.getQueryOptimizeGoal() : 1);
        target.setLobThreshold(source.getLobThreshold() != null ? source.getLobThreshold() : 32768);

        target.setQueryTimeout(source.getQueryTimeout());
        target.setFetchSize(source.getFetchSize());
    }

    private void applyPoolSizeChange() {
        if (dataSource instanceof HikariDataSource hikariDS) {
            hikariDS.setMaximumPoolSize(currentConfiguration.getMaximumPoolSize());
            hikariDS.setMinimumIdle(currentConfiguration.getMinimumIdle());
            LOG.info("Applied pool size changes to HikariCP datasource");
        } else {
            LOG.warn("DataSource is not HikariDataSource, cannot apply pool size changes");
        }
    }

    private void applyPoolTimeoutChanges() {
        if (dataSource instanceof HikariDataSource hikariDS) {
            hikariDS.setConnectionTimeout(currentConfiguration.getConnectionTimeout());
            hikariDS.setIdleTimeout(currentConfiguration.getIdleTimeout());
            hikariDS.setMaxLifetime(currentConfiguration.getMaxLifetime());
            hikariDS.setLeakDetectionThreshold(currentConfiguration.getLeakDetectionThreshold());
            LOG.info("Applied timeout changes to HikariCP datasource");
        } else {
            LOG.warn("DataSource is not HikariDataSource, cannot apply timeout changes");
        }
    }

    private void refreshConnectionPool() {
        if (dataSource instanceof HikariDataSource hikariDS) {
            hikariDS.getHikariPoolMXBean().softEvictConnections();
            LOG.info("Connection pool refreshed - new connections will use updated driver properties");
        } else {
            LOG.warn("DataSource is not HikariDataSource, cannot refresh connection pool");
        }
    }

    private void logCurrentConfiguration(String message) {
        // Using Lombok-generated getters
        LOG.info("{}: Pool[max={}, min={}, connTimeout={}, idleTimeout={}, maxLife={}, leak={}], " +
                        "AutoCommit={}, AS400[thread={}, naming='{}', cursor={}, extended={}, cache={}, block={}], " +
                        "Network[sockTimeout={}, keepAlive={}, tcpNoDelay={}], Performance[prefetch={}, optimize={}, lobThreshold={}], " +
                        "Query[timeout={}, fetchSize={}]",
                message,
                currentConfiguration.getMaximumPoolSize(), currentConfiguration.getMinimumIdle(), currentConfiguration.getConnectionTimeout(),
                currentConfiguration.getIdleTimeout(), currentConfiguration.getMaxLifetime(), currentConfiguration.getLeakDetectionThreshold(),
                currentConfiguration.getAutoCommit(), currentConfiguration.getThreadUsed(), currentConfiguration.getNaming(),
                currentConfiguration.getCursorHold(), currentConfiguration.getExtendedDynamic(), currentConfiguration.getPackageCache(),
                currentConfiguration.getBlockSize(), currentConfiguration.getSocketTimeout(), currentConfiguration.getSocketKeepAlive(),
                currentConfiguration.getTcpNoDelay(), currentConfiguration.getPrefetch(), currentConfiguration.getQueryOptimizeGoal(),
                currentConfiguration.getLobThreshold(), currentConfiguration.getQueryTimeout(), currentConfiguration.getFetchSize());
    }

    // Updated request record to include the new performance properties and remove fetchSize
    public record JdbcConfigurationUpdateRequest(
            // HikariCP Connection Pool Performance Settings
            Integer maximumPoolSize,
            Integer minimumIdle,
            Integer connectionTimeout,
            Integer idleTimeout,
            Integer maxLifetime,
            Integer leakDetectionThreshold,
            Boolean autoCommit,

            // AS/400 JDBC Driver Performance Properties
            String naming,
            String libraries,
            Boolean threadUsed,
            Boolean cursorHold,
            Boolean extendedDynamic,
            Boolean packageCache,
            Integer blockSize,
            Integer socketTimeout,
            Boolean socketKeepAlive,
            Boolean tcpNoDelay,

            // NEW: Critical AS/400 performance properties
            Boolean prefetch,
            Integer queryOptimizeGoal,
            Integer lobThreshold,

            // Query Performance Settings (removed fetchSize since blockSize takes precedence)
            Integer queryTimeout,
            Integer fetchSize
    ) {}
}
