package com.zodd.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

/**
 * Just a list of package matchers, no glob/regex yet
 */
public class PackageList implements Iterable<String> {

    private final List<String> packages;

    public PackageList() {
        this(new ArrayList<>());
    }

    public PackageList(String... packages) {
        this.packages = Arrays.stream(packages).collect(Collectors.toList());
    }

    public PackageList(List<String> packages) {
        this.packages = new ArrayList<>(packages);
    }

    public void add(String ppackage) {
        this.packages.add(ppackage);
    }

    public boolean isEmpty() {
        return this.packages.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return packages.iterator();
    }

    @Override
    public String toString() {
        return String.join(",", this.packages);
    }
}
