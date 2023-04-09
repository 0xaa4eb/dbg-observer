package com.zodd.agent;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MethodCallWallTimeData {

    private final int methodId;
    private final long millisEpochTime;
    private final long nanos;
}
