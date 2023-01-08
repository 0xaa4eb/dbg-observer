package com.zodd.agent;

public class AgentContext {

    private static final AgentContext instance = new AgentContext();

    private static volatile boolean agentLoaded = false;

    private AgentContext() {

    }

    public static AgentContext getInstance() {
        return instance;
    }

    public static boolean isLoaded() {
        return agentLoaded;
    }

    public static void setLoaded() {
        agentLoaded = true;
    }
}
