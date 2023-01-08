package com.zodd.agent;

public interface MeasurementStorage {

    void store(MethodCallLatencyMeasurement measurement);
}
