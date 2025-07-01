package com.lkww.controller;

import com.lkww.config.JdbcConfiguration;
import com.lkww.service.JdbcConfigurationService;
import com.lkww.service.JdbcConfigurationService.JdbcConfigurationUpdateRequest;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/config")
@Tag(name = "Configuration", description = "JDBC Configuration management endpoints")
public class ConfigController {
    
    @Inject
    private JdbcConfigurationService jdbcConfigurationService;
    
    @Get
    @Operation(summary = "Get current JDBC configuration", description = "Retrieves the current JDBC configuration settings")
    public JdbcConfiguration getConfig() {
        return jdbcConfigurationService.getCurrentConfiguration();
    }
    
    @Put
    @Operation(summary = "Update JDBC configuration", description = "Updates the JDBC configuration with provided values")
    public JdbcConfiguration updateConfig(@Body JdbcConfigurationUpdateRequest request) {
        return jdbcConfigurationService.updateConfiguration(request);
    }
    
    @Put("/{fieldName}")
    @Operation(summary = "Update single configuration field", description = "Updates a single field in the JDBC configuration")
    public JdbcConfiguration updateSingleField(@PathVariable String fieldName, @Body String value) {
        return jdbcConfigurationService.updateSingleField(fieldName, value);
    }
}