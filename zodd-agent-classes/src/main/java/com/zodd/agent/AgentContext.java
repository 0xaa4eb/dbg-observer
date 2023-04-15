package com.zodd.agent;

public class AgentContext {

    private static AgentContext instance;
    private static volatile boolean agentLoaded = false;

    private final ProfileDataStorage resultPrinter;
    private final MethodRepository methodRepository;

    private AgentContext(Settings settings) throws Exception {
        this.methodRepository = new MethodRepository();
        this.resultPrinter = new FileProfileDataStorage(settings, methodRepository);
    }

    public static AgentContext getInstance() {
        return instance;
    }

    public static void initInstance(Settings settings) throws Exception {
        instance = new AgentContext(settings);
        setLoaded();
    }

    public static boolean isLoaded() {
        return agentLoaded;
    }

    public static void setLoaded() {
        agentLoaded = true;
    }

    public ProfileDataStorage getResultPrinter() {
        return resultPrinter;
    }

    public MethodRepository getMethodRepository() {
        return methodRepository;
    }
}
