package com.lkww.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import com.lkww.service.PerformanceMetricsService;

@Controller("/metrics")
@Tag(name = "Metrics", description = "Performance metrics and monitoring endpoints")
public class MetricsController {
    
    @Inject
    private PerformanceMetricsService performanceMetricsService;
    
    @Get
    @Operation(summary = "Get performance metrics", description = "Retrieves current performance metrics and statistics")
    public PerformanceMetricsService.PerformanceMetrics getMetrics() {
        return performanceMetricsService.getCurrentMetrics();
    }
}