package com.zodd.agent.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Builder;

@Builder
public class NamedThreadFactory implements ThreadFactory {

    private final String name;
    private final boolean daemon;
    private final AtomicLong nextIndex = new AtomicLong(-1);

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, name + "-" + nextIndex.incrementAndGet());
        t.setDaemon(daemon);
        return t;
    }
}
