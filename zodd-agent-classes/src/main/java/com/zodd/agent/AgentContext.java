package com.zodd.agent;

public class AgentContext {

    private static AgentContext instance;
    private static volatile boolean agentLoaded = false;

    private final MeasurementStorage resultPrinter;

    private AgentContext(Settings settings) {
        resultPrinter = new StdOutMeasurementPrinter(settings, MethodRepository.getInstance());
    }

    public static AgentContext getInstance() {
        return instance;
    }

    public static void initInstance(Settings settings) {
        instance = new AgentContext(settings);
        setLoaded();
    }

    public static boolean isLoaded() {
        return agentLoaded;
    }

    public static void setLoaded() {
        agentLoaded = true;
    }

    public MeasurementStorage getResultPrinter() {
        return resultPrinter;
    }
}
