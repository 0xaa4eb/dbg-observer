package com.zodd.agent.util;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Prints byte size to human-readable format
 */
public class Duration {

    private static final long MICROSECOND_THRESHOLD = 1000;
    private static final long MILLISECOND_THRESHOLD = MICROSECOND_THRESHOLD * 1000;
    private static final long SECONDS_THRESHOLD = MILLISECOND_THRESHOLD * 1000;
    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("#.###");

    private final long nanos;

    public Duration(long nanos) {
        this.nanos = nanos;
    }

    public static Duration ofMicros(long micros) {
        return new Duration(TimeUnit.MICROSECONDS.toNanos(micros));
    }

    public static String print(long nanos) {
        if (nanos >= SECONDS_THRESHOLD) return formatSize(nanos, SECONDS_THRESHOLD, "s");
        if (nanos >= MILLISECOND_THRESHOLD) return formatSize(nanos, MILLISECOND_THRESHOLD, "ms");
        if (nanos >= MICROSECOND_THRESHOLD) return formatSize(nanos, MICROSECOND_THRESHOLD, "us");
        return formatSize(nanos, 1, "ns");
    }

    private static String formatSize(long size, long divider, String unitName) {
        return DEC_FORMAT.format((double) size / divider) + " " + unitName;
    }

    @Override
    public String toString() {
        return print(nanos);
    }
}
