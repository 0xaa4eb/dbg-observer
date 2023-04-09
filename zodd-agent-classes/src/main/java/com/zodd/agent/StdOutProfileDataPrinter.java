package com.zodd.agent;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.Executors;

import com.zodd.agent.util.NamedThreadFactory;
import org.jctools.queues.MpscLinkedQueue;

public class StdOutProfileDataPrinter implements ProfileDataStorage {

    private final Queue<MethodCallWallTimeData> queue = new MpscLinkedQueue<>();
    private final MethodRepository methodRepository;

    public StdOutProfileDataPrinter(Settings settings, MethodRepository methodRepository) {
        this.methodRepository = methodRepository;

        Executors.newFixedThreadPool(
                1,
                NamedThreadFactory.builder()
                        .name("zodd-stdout")
                        .daemon(true)
                        .build()
        ).submit(
            () -> {
                while (!Thread.currentThread().isInterrupted()) {
                    MethodCallWallTimeData result = queue.poll();
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

    public void store(MethodCallWallTimeData measurement) {
        queue.add(measurement);
    }
}
