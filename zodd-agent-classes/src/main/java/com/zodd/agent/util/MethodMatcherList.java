package com.zodd.agent.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.zodd.agent.Method;


public class MethodMatcherList implements Iterable<MethodMatcher> {

    private final List<MethodMatcher> methods;

    private MethodMatcherList(List<MethodMatcher> methods) {
        this.methods = methods;
    }

    public static MethodMatcherList parse(String text) {
        return new MethodMatcherList(CommaSeparatedList.parse(text).stream().map(MethodMatcher::parse).collect(Collectors.toList()));
    }

    public static MethodMatcherList of(MethodMatcher matcher) {
        return new MethodMatcherList(Collections.singletonList(matcher));
    }

    public boolean isEmpty() {
        return this.methods.isEmpty();
    }

    public boolean anyMatch(Method description) {
        return methods.stream().anyMatch(matcher -> matcher.matches(description));
    }

    public boolean useSuperTypes() {
        return methods.stream().anyMatch(MethodMatcher::matchesSuperTypes);
    }

    @Override
    public String toString() {
        return methods.toString();
    }

    @NotNull
    @Override
    public Iterator<MethodMatcher> iterator() {
        return methods.iterator();
    }
}
