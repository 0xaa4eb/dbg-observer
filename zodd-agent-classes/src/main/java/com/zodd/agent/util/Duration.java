package com.zodd.agent.util;

import java.text.DecimalFormat;

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

    public static String of(long size) {
        if (size >= SECONDS_THRESHOLD) return formatSize(size, SECONDS_THRESHOLD, "s");
        if (size >= MILLISECOND_THRESHOLD) return formatSize(size, MILLISECOND_THRESHOLD, "ms");
        if (size >= MICROSECOND_THRESHOLD) return formatSize(size, MICROSECOND_THRESHOLD, "us");
        return formatSize(size, 1, "ns");
    }

    private static String formatSize(long size, long divider, String unitName) {
        return DEC_FORMAT.format((double) size / divider) + " " + unitName;
    }

    @Override
    public String toString() {
        return of(nanos);
    }
}
