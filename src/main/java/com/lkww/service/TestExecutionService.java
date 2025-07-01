package com.lkww.service;

import com.lkww.config.JdbcConfiguration;
import com.lkww.controller.TestController.TestRequest;
import io.micrometer.core.instrument.Timer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class TestExecutionService {
    
    private static final Logger LOG = LoggerFactory.getLogger(TestExecutionService.class);
    private static final String DEFAULT_QUERY = "SELECT 1 FROM SYSIBM.SYSDUMMY1";
    private static final int ROW_CHECK_INTERVAL = 10000;
    
    @Inject
    private DataSource dataSource;

    @Inject
    private PerformanceMetricsService performanceMetricsService;
    
    @Inject
    private JdbcConfigurationService jdbcConfigurationService;
    
    private ExecutorService executorService;
    private final AtomicBoolean testRunning = new AtomicBoolean(false);
    private CompletableFuture<TestResult> currentTest;
    private TestResult lastResult;
    
    public String startTest(TestRequest request) {
        if (testRunning.get()) {
            throw new IllegalStateException("Test is already running");
        }
        
        String testId = UUID.randomUUID().toString();
        executorService = Executors.newFixedThreadPool(request.concurrency());
        testRunning.set(true);
        
        LOG.info("Starting test {} with concurrency: {}, duration: {}s, query: {}", 
                testId, request.concurrency(), request.duration(), request.query());
        
        currentTest = CompletableFuture.supplyAsync(() -> executeTest(testId, request));
        
        return testId;
    }
    
    public void stopTest() {
        if (testRunning.get()) {
            testRunning.set(false);
            if (executorService != null) {
                executorService.shutdownNow();
            }
            LOG.info("Test execution stopped");
        }
    }
    
    public Object getResults() {
        if (currentTest != null && currentTest.isDone()) {
            try {
                lastResult = currentTest.get();
                return lastResult;
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Error getting test results", e);
                return new TestResult(null, 0, 0, 0, List.of("Error: " + e.getMessage()));
            }
        } else if (lastResult != null) {
            return lastResult;
        } else {
            return new TestResult(null, 0, 0, 0, List.of("No test results available"));
        }
    }
    
    private TestResult executeTest(String testId, TestRequest request) {
        Instant startTime = Instant.now();
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong errorCount = new AtomicLong(0);
        List<String> errors = new CopyOnWriteArrayList<>();
        
        String query = getQueryToExecute(request);
        List<CompletableFuture<Void>> tasks = createWorkerTasks(request, startTime, query, successCount, errorCount, errors);
        
        waitForTasksToComplete(tasks);
        cleanupResources();
        
        return createTestResult(testId, successCount, errorCount, errors);
    }
    
    private String getQueryToExecute(TestRequest request) {
        return request.query() != null ? request.query() : getDefaultQuery();
    }
    
    private List<CompletableFuture<Void>> createWorkerTasks(TestRequest request, Instant startTime, String query,
                                                             AtomicLong successCount, AtomicLong errorCount, List<String> errors) {
        return IntStream.range(0, request.concurrency())
                .mapToObj(i -> CompletableFuture.runAsync(() -> 
                    executeWorkerTask(startTime, request.duration(), query, successCount, errorCount, errors), executorService))
                .toList();
    }
    
    private void executeWorkerTask(Instant startTime, int durationSeconds, String query,
                                   AtomicLong successCount, AtomicLong errorCount, List<String> errors) {
        Instant endTime = startTime.plusSeconds(durationSeconds);
        
        while (shouldContinueExecution(endTime)) {
            Timer.Sample sample = performanceMetricsService.startQueryTimer();
            
            try {
                executeQueryWithMetrics(query, sample, successCount);
            } catch (SQLException e) {
                handleQueryError(sample, e, errorCount, errors);
            }
            
            if (!pauseBetweenQueries()) {
                break;
            }
        }
    }
    
    private boolean shouldContinueExecution(Instant endTime) {
        return testRunning.get() && Instant.now().isBefore(endTime);
    }
    
    private void executeQueryWithMetrics(String query, Timer.Sample sample, AtomicLong successCount) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = prepareStatement(connection, query)) {
            
            long rowsProcessed = executeQueryAndProcessResults(stmt);
            LOG.debug("Processed {} rows for query execution", rowsProcessed);
            
            performanceMetricsService.recordQuerySuccess(sample);
            successCount.incrementAndGet();
        }
    }
    
    private PreparedStatement prepareStatement(Connection connection, String query) throws SQLException {
        JdbcConfiguration config = jdbcConfigurationService.getCurrentConfiguration();
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setFetchSize(config.getFetchSize());
        
        if (config.getQueryTimeout() > 0) {
            stmt.setQueryTimeout(config.getQueryTimeout());
        }
        
        return stmt;
    }
    
    private long executeQueryAndProcessResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            long rowsProcessed = 0;
            
            while (rs.next() && testRunning.get()) {
                rowsProcessed++;
                
                if (shouldCheckForInterruption(rowsProcessed)) {
                    break;
                }
            }
            
            return rowsProcessed;
        }
    }
    
    private boolean shouldCheckForInterruption(long rowsProcessed) {
        return rowsProcessed % ROW_CHECK_INTERVAL == 0 && Thread.currentThread().isInterrupted();
    }
    
    private void handleQueryError(Timer.Sample sample, SQLException e, AtomicLong errorCount, List<String> errors) {
        String errorMsg = "SQL Error: " + e.getMessage();
        performanceMetricsService.recordQueryError(sample, errorMsg);
        errorCount.incrementAndGet();
        errors.add(errorMsg);
        LOG.warn("Query execution failed", e);
    }
    
    private boolean pauseBetweenQueries() {
        try {
            Thread.sleep(10);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private void waitForTasksToComplete(List<CompletableFuture<Void>> tasks) {
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
    }
    
    private void cleanupResources() {
        testRunning.set(false);
        executorService.shutdown();
    }
    
    private TestResult createTestResult(String testId, AtomicLong successCount, AtomicLong errorCount, List<String> errors) {
        long totalQueries = successCount.get() + errorCount.get();
        
        LOG.info("Test {} completed. Total queries: {}, Success: {}, Errors: {}", 
                testId, totalQueries, successCount.get(), errorCount.get());
        
        return new TestResult(testId, totalQueries, successCount.get(), errorCount.get(), errors);
    }
    
    private String getDefaultQuery() {
        return DEFAULT_QUERY;
    }
    
    public record TestResult(
            String testId,
            long totalQueries,
            long successfulQueries,
            long failedQueries,
            List<String> errors
    ) {}
}

class IntStream {
    public static java.util.stream.IntStream range(int start, int end) {
        return java.util.stream.IntStream.range(start, end);
    }
}