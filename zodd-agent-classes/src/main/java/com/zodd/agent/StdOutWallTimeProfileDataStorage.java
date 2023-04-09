package com.zodd.agent;

import com.zodd.agent.util.Duration;
import com.zodd.agent.util.NamedThreadFactory;
import org.HdrHistogram.Histogram;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StdOutWallTimeProfileDataStorage implements ProfileDataStorage {

    private final Map<Key, WallTimeProfiler> profilers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService;
    private final MethodRepository methodRepository;

    public StdOutWallTimeProfileDataStorage(Settings settings, MethodRepository methodRepository) {
        this.methodRepository = methodRepository;

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory.builder().name("zodd-stdout").daemon(true).build());
        scheduledExecutorService.scheduleAtFixedRate(
                () -> profilers.values().forEach(WallTimeProfiler::resetAndPrintState),
                1,
                1,
                TimeUnit.SECONDS
        );
    }

    public void store(MethodCallWallTimeData measurement) {
        Key key = new Key(Thread.currentThread(), measurement.getMethodId());
        WallTimeProfiler profiler = profilers.get(key);
        if (profiler != null) {
            profiler.record(measurement.getNanos());
        } else {
            profilers.put(key, new WallTimeProfiler(methodRepository.get(measurement.getMethodId())));
        }
    }

    private static class Key {
        private final Thread thread;
        private final int methodId;

        private Key(Thread thread, int methodId) {
            this.thread = thread;
            this.methodId = methodId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return methodId == key.methodId && Objects.equals(thread, key.thread);
        }

        @Override
        public int hashCode() {
            return Objects.hash(thread, methodId);
        }
    }

    private static class WallTimeProfiler {

        private final Method method;
        private volatile Histogram histogram;

        private WallTimeProfiler(Method method) {
            this.method = method;
            this.histogram = new Histogram(1, TimeUnit.MINUTES.toMicros(5), 5);
        }

        public synchronized void record(long nanoTime) {
            histogram.recordValue(TimeUnit.NANOSECONDS.toMicros(nanoTime));
        }

        public void resetAndPrintState() {
            Histogram currentHistogram = this.histogram;
            if (currentHistogram.getTotalCount() > 0L) {
                this.histogram = new Histogram(1, TimeUnit.MINUTES.toMicros(5), 5);

                System.out.println(
                        "method: " + method.toString() +
                        ", count: " + currentHistogram.getTotalCount() +
                        ", median: " + new Duration(currentHistogram.getValueAtPercentile(50.0)) +
                        ", p90: " + new Duration(currentHistogram.getValueAtPercentile(90.0)) +
                        ", p99: " + new Duration(currentHistogram.getValueAtPercentile(99.0)) +
                        ", max: " + new Duration(currentHistogram.getMaxValue()));
            }
        }
    }
}
