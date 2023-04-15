package com.zodd.agent;

import net.bytebuddy.asm.Advice;

public class CallTimeProfileAdvice {

    @Advice.OnMethodEnter
    static void enter(
            @MethodId int methodId,
            @Advice.Local("timestamp") long timestamp,
            @Advice.Local("epochTimestamp") long epochTimestamp) {
        timestamp = System.nanoTime();
    }

    @Advice.OnMethodExit
    static void exit(
            @MethodId int methodId,
            @Advice.Local("timestamp") long timestamp,
            @Advice.Local("epochTimestamp") long epochTimestamp) {
        long durationNanos = System.nanoTime() - timestamp;

        AgentContext.getInstance().getResultPrinter()
            .store(MethodCallTimeData.builder()
                .nanos(durationNanos)
                .millisEpochTime(epochTimestamp)
                .methodId(methodId)
                .build());
    }
}
