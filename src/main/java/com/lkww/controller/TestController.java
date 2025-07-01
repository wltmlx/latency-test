package com.lkww.controller;

import com.lkww.service.TestExecutionService;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/test")
@Tag(name = "Testing", description = "Database latency and performance testing endpoints")
public class TestController {
    
    @Inject
    private TestExecutionService testExecutionService;
    
    @Post("/start")
    @Operation(summary = "Start performance test", description = "Initiates a new database performance test with specified parameters")
    public TestResponse startTest(@Body TestRequest request) {
        String testId = testExecutionService.startTest(request);
        return new TestResponse("started", testId);
    }
    
    @Post("/stop")
    @Operation(summary = "Stop running test", description = "Stops the currently running performance test")
    public TestResponse stopTest() {
        testExecutionService.stopTest();
        return new TestResponse("stopped", null);
    }
    
    @Get("/results")
    @Operation(summary = "Get test results", description = "Retrieves the results of the most recent performance test")
    public Object getResults() {
        return testExecutionService.getResults();
    }
    
    @Serdeable
    public record TestRequest(
            String queryType,
            int duration,
            int concurrency,
            String query
    ) {}
    
    @Serdeable
    public record TestResponse(String status, String testId) {}
}