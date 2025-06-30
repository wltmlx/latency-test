package com.lkww.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class PerformanceMetricsService {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceMetricsService.class);

    private final Timer queryTimer;
    private final Counter queryCounter;
    private final Counter errorCounter;
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong idleConnections = new AtomicLong(0);
    private final AtomicReference<Double> currentQps = new AtomicReference<>(0.0);
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicReference<Instant> lastQpsCalculation = new AtomicReference<>(Instant.now());

    public PerformanceMetricsService(MeterRegistry meterRegistry) {
        this.queryTimer = Timer.builder("jdbc.query.duration")
                .description("Time taken to execute JDBC queries")
                .register(meterRegistry);

        this.queryCounter = Counter.builder("jdbc.query.total")
                .description("Total number of JDBC queries executed")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("jdbc.error.total")
                .description("Total number of JDBC errors")
                .register(meterRegistry);

        Gauge.builder("jdbc.connections.active", activeConnections, AtomicLong::get)
                .description("Number of active JDBC connections")
                .register(meterRegistry);

        Gauge.builder("jdbc.connections.idle", idleConnections, AtomicLong::get)
                .description("Number of idle JDBC connections")
                .register(meterRegistry);

        Gauge.builder("jdbc.qps", currentQps, AtomicReference::get)
                .description("Current queries per second")
                .register(meterRegistry);
    }

    public Timer.Sample startQueryTimer() {
        return Timer.start();
    }

    public void recordQuerySuccess(Timer.Sample sample) {
        sample.stop(queryTimer);
        queryCounter.increment();
        totalQueries.incrementAndGet();
        updateQps();

        LOG.debug("Query executed successfully. Total queries: {}, Current QPS: {}",
                totalQueries.get(), currentQps.get());
    }

    public void recordQueryError(Timer.Sample sample, String errorMessage) {
        sample.stop(queryTimer);
        errorCounter.increment();

        LOG.warn("Query execution failed: {}", errorMessage);
    }

    public void updateConnectionMetrics(long active, long idle) {
        activeConnections.set(active);
        idleConnections.set(idle);

        LOG.debug("Connection metrics updated - Active: {}, Idle: {}", active, idle);
    }

    private void updateQps() {
        Instant now = Instant.now();
        Instant lastCalculation = lastQpsCalculation.get();
        Duration timeSinceLastCalculation = Duration.between(lastCalculation, now);

        if (timeSinceLastCalculation.toSeconds() >= 1) {
            if (lastQpsCalculation.compareAndSet(lastCalculation, now)) {
                double qps = 1.0 / timeSinceLastCalculation.toMillis() * 1000.0;
                currentQps.set(qps);
            }
        }
    }

    public PerformanceMetrics getCurrentMetrics() {
        return new PerformanceMetrics(
                currentQps.get(),
                totalQueries.get(),
                errorCounter.count(),
                activeConnections.get(),
                idleConnections.get(),
                queryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS),
                queryTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS)
        );
    }

    public record PerformanceMetrics(
            double queriesPerSecond,
            long totalQueries,
            double totalErrors,
            long activeConnections,
            long idleConnections,
            double averageResponseTime,
            double maxResponseTime
    ) {}
}