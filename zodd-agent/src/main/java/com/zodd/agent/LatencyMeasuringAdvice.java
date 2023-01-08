package com.zodd.agent;

import net.bytebuddy.asm.Advice;

public class MethodCallRecordingAdvice {

    @Advice.OnMethodEnter
    static void enter(
            @MethodId int methodId,
            @Advice.Local("callId") long callId) {
        System.out.println("**** Method enter");
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void exit(
            @MethodId int methodId,
            @Advice.Local("callId") long callId) {
        System.out.println("**** Method exit");
    }
}
