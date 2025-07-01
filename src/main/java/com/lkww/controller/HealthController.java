package com.lkww.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Controller("/health")
@Tag(name = "Health", description = "Application and database health check endpoints")
public class HealthController {
    
    @Inject
    private DataSource dataSource;
    
    @Get
    @Operation(summary = "Check application health", description = "Returns the health status of the application and database connection")
    public HealthStatus health() {
        boolean dbHealthy = checkDatabaseHealth();
        boolean overall = dbHealthy;
        
        return new HealthStatus(
                overall ? "UP" : "DOWN",
                new DatabaseHealth(dbHealthy ? "UP" : "DOWN")
        );
    }
    
    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    @Serdeable
    public record HealthStatus(String status, DatabaseHealth database) {}
    
    @Serdeable
    public record DatabaseHealth(String status) {}
}