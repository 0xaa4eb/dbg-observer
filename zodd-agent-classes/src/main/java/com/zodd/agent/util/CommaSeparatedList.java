package com.zodd.agent.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommaSeparatedList {

    public static List<String> parse(String text) {
        String[] split = text.trim().split(",");
        if (split.length == 1 && split[0].trim().isEmpty()) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(split).map(String::trim).collect(Collectors.toList());
        }
    }
}
