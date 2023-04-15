package com.zodd.agent;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Method {

    private final long id;
    private final String name;
    private final Type declaringType;

    public String toShortString() {
        return declaringType.getName() + "." + name;
    }
}
