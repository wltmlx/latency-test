package com.lkww.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Controller("/health")
public class HealthController {
    
    @Inject
    private DataSource dataSource;
    
    @Get
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