package com.zodd.agent;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.Executors;

import org.jctools.queues.MpscLinkedQueue;

public class ResultPrinter implements MeasurementStorage {

    private final Queue<LatencyMeasureResult> queue = new MpscLinkedQueue<>();
    private final MethodRepository methodRepository;

    public ResultPrinter(Settings settings, MethodRepository methodRepository) {
        this.methodRepository = methodRepository;

        Executors.newFixedThreadPool(1).submit(
            () -> {
                while (!Thread.currentThread().isInterrupted()) {
                    LatencyMeasureResult result = queue.poll();
                    if (result == null) {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        int methodId = result.getMethodId();
                        Method method = methodRepository.get(methodId);
                        System.out.println(new Date(result.getMillisEpochTime()) + " " + method + " " + " took " + result.getNanos() + " ns");
                    }
                }
            }
        );
    }

    public void store(LatencyMeasureResult result) {
        queue.add(result);
    }
}
