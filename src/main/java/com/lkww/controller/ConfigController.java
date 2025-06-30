package com.lkww.controller;

import com.lkww.config.JdbcConfiguration;
import com.lkww.service.JdbcConfigurationService;
import com.lkww.service.JdbcConfigurationService.JdbcConfigurationUpdateRequest;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

@Controller("/config")
public class ConfigController {
    
    @Inject
    private JdbcConfigurationService jdbcConfigurationService;
    
    @Get
    public JdbcConfiguration getConfig() {
        return jdbcConfigurationService.getCurrentConfiguration();
    }
    
    @Put
    public JdbcConfiguration updateConfig(@Body JdbcConfigurationUpdateRequest request) {
        return jdbcConfigurationService.updateConfiguration(request);
    }
    
    @Put("/{fieldName}")
    public JdbcConfiguration updateSingleField(@PathVariable String fieldName, @Body String value) {
        return jdbcConfigurationService.updateSingleField(fieldName, value);
    }
}