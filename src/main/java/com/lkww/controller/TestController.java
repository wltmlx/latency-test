package com.lkww.controller;

import com.lkww.service.TestExecutionService;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;

@Controller("/test")
public class TestController {
    
    @Inject
    private TestExecutionService testExecutionService;
    
    @Post("/start")
    public TestResponse startTest(@Body TestRequest request) {
        String testId = testExecutionService.startTest(request);
        return new TestResponse("started", testId);
    }
    
    @Post("/stop")
    public TestResponse stopTest() {
        testExecutionService.stopTest();
        return new TestResponse("stopped", null);
    }
    
    @Get("/results")
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