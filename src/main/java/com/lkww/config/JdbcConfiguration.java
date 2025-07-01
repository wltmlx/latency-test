package com.lkww.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("performance.test")
public class JdbcConfiguration {

    // HikariCP Connection Pool Performance Settings
    private Integer maximumPoolSize = 10;
    private Integer minimumIdle = 5;
    private Integer connectionTimeout = 30000;
    private Integer idleTimeout = 600000;
    private Integer maxLifetime = 1800000;
    private Integer leakDetectionThreshold = 60000;
    private Boolean autoCommit = true;

    // AS/400 JDBC Driver Performance Properties
    private String naming = "sql";
    private String libraries = "";
    private Boolean threadUsed = true;
    private Boolean cursorHold = true;
    private Boolean extendedDynamic = true;
    private Boolean packageCache = true;
    private Integer blockSize = 512;
    private Integer socketTimeout = 300000;
    private Boolean socketKeepAlive = true;
    private Boolean tcpNoDelay = true;

    // NEW: Critical AS/400 performance properties we discussed
    private Boolean prefetch = true;
    private Integer queryOptimizeGoal = 1;
    private Integer lobThreshold = 32768;

    // Query Performance Settings (removed fetchSize since blockSize takes precedence)
    private Integer queryTimeout = 0;
    private Integer fetchSize = 1000;
}
