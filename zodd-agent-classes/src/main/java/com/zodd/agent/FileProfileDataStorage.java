package com.zodd.agent;

import com.zodd.agent.util.Duration;
import com.zodd.agent.util.NamedThreadFactory;
import org.HdrHistogram.Histogram;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileProfileDataStorage implements ProfileDataStorage {

    private final Map<Key, CallTimeProfiler> profilers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService;
    private final MethodRepository methodRepository;

    public FileProfileDataStorage(Settings settings, MethodRepository methodRepository) throws IOException {
        this.methodRepository = methodRepository;

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory.builder().name("zodd-stdout").daemon(true).build());
        scheduledExecutorService.scheduleAtFixedRate(
                new FileDumper(new File(settings.getFile())),
                1,
                1,
                TimeUnit.SECONDS
        );
    }

    public void store(MethodCallTimeData measurement) {
        Key key = new Key(Thread.currentThread(), measurement.getMethodId());
        CallTimeProfiler profiler = profilers.get(key);
        if (profiler != null) {
            profiler.record(measurement.getNanos());
        } else {
            profilers.put(key, new CallTimeProfiler(methodRepository.get(measurement.getMethodId())));
        }
    }

    private class FileDumper implements Runnable {

        private final BufferedWriter fileWriter;

        private FileDumper(File file) throws IOException {
            fileWriter = new BufferedWriter(new FileWriter(file, true));
        }

        @Override
        public void run() {
            try {
                for (Map.Entry<Key, CallTimeProfiler> profiler : profilers.entrySet()) {
                    if (!profiler.getValue().hasSomething()) {
                        continue;
                    }
                    Method method = methodRepository.get(profiler.getKey().methodId);
                    Histogram histogram = profiler.getValue().resetAndGetState();

                    fileWriter.write(
                        LocalDateTime.now() +
                            " " +
                            profiler.getKey().thread +
                            " method: " + method.toShortString() +
                            ", count: " + histogram.getTotalCount() +
                            ", median: " + new Duration(histogram.getValueAtPercentile(50.0)) +
                            ", p90: " + new Duration(histogram.getValueAtPercentile(90.0)) +
                            ", p99: " + new Duration(histogram.getValueAtPercentile(99.0)) +
                            ", max: " + new Duration(histogram.getMaxValue()));

                }
                fileWriter.flush();
            } catch (IOException e) {
                log.error("Failed to write profile data", e);
                throw new RuntimeException(e);
            }
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

    private static class CallTimeProfiler {

        private final Method method;
        private volatile Histogram histogram;

        private CallTimeProfiler(Method method) {
            this.method = method;
            this.histogram = new Histogram(1, TimeUnit.MINUTES.toMicros(5), 5);
        }

        public synchronized void record(long nanoTime) {
            histogram.recordValue(TimeUnit.NANOSECONDS.toMicros(nanoTime));
        }

        public boolean hasSomething() {
            return this.histogram.getTotalCount() > 0L;
        }

        public Histogram resetAndGetState() {
            Histogram currentHistogram = this.histogram;
            this.histogram = new Histogram(1, TimeUnit.MINUTES.toMicros(5), 5);
            return currentHistogram;
        }
    }
}
