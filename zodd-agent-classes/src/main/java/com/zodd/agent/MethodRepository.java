package com.zodd.agent;

import com.zodd.agent.util.ConcurrentArrayList;

public class MethodRepository {

    private final ConcurrentArrayList<Method> methods = new ConcurrentArrayList<>(64_000);

    public MethodRepository() {
    }

    public Method get(int id) {
        return methods.get(id);
    }

    public int putAndGetId(Method method) {
        return methods.add(method);
    }
}
