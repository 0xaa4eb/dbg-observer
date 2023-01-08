package com.zodd.agent;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * Domain class for all java types. All type-related logic uses this class. It is also
 * written to the output recording file and later is read in UI
 */
@Builder
@AllArgsConstructor
@ToString
public class Type {

    private static final Type UNKNOWN = Type.builder().name("Unknown").id(-1).build();

    private final long id;
    private final String name;
    @Builder.Default
    private final Set<String> superTypeNames = new HashSet<>();
    @Builder.Default
    private final Set<String> superTypeSimpleNames = new HashSet<>();

    public static Type unknown() {
        return UNKNOWN;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getSuperTypeNames() {
        return superTypeNames;
    }

    public Set<String> getSuperTypeSimpleNames() {
        return superTypeSimpleNames;
    }
}
