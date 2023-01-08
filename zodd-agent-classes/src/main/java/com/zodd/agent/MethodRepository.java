package com.zodd.agent;

import com.zodd.agent.util.ConcurrentArrayList;

public class MethodRepository {

    private static final MethodRepository INSTANCE = new MethodRepository();

    private final ConcurrentArrayList<Method> methods = new ConcurrentArrayList<>(64_000);

    private MethodRepository() {
    }

    public static MethodRepository getInstance() {
        return INSTANCE;
    }

    public Method get(int id) {
        return methods.get(id);
    }

    public int putAndGetId(Method method) {
        return methods.add(method);
    }
}
