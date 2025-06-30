package com.lkww.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import com.lkww.service.PerformanceMetricsService;

@Controller("/metrics")
public class MetricsController {
    
    @Inject
    private PerformanceMetricsService performanceMetricsService;
    
    @Get
    public PerformanceMetricsService.PerformanceMetrics getMetrics() {
        return performanceMetricsService.getCurrentMetrics();
    }
}