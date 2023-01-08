package com.zodd.agent.util;

import java.util.concurrent.atomic.AtomicLong;

import com.zodd.agent.LoggingSettings;
import com.zodd.agent.Method;
import com.zodd.agent.Type;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;

/**
 * Converts byte buddy method description to internal domain class {@link Method}
 */
@Slf4j
public class ByteBuddyMethodResolver {

    private static final AtomicLong idGenerator = new AtomicLong();

    private final ByteBuddyTypeResolver typeResolver = new ByteBuddyTypeResolver();

    public Method resolve(MethodDescription description) {
        Type declaringType = typeResolver.resolve(description.getDeclaringType().asGenericType());

        Method resolved = Method.builder()
                .id(idGenerator.incrementAndGet())
                .name(description.isConstructor() ? "<init>" : description.getActualName())
                .declaringType(declaringType)
                .build();

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Resolved {} to {}", description, resolved);
        }
        return resolved;
    }
}
