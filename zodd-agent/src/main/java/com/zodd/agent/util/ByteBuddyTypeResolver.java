package com.zodd.agent.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.zodd.agent.ClassUtils;
import com.zodd.agent.LoggingSettings;
import com.zodd.agent.Type;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;

/**
 * Converts byte buddy type description to internal domain class {@link Type}
 */
@Slf4j
public class ByteBuddyTypeResolver {

    private static final AtomicLong typeIdGenerator = new AtomicLong(0L);

    private final Map<Class<?>, Type> types = new ConcurrentHashMap<>();

    public static ByteBuddyTypeResolver getInstance() {
        return InstanceHolder.context;
    }

    @NotNull
    public Type get(Object object) {
        Type resolvedType;

        if (object != null) {
            resolvedType = types.computeIfAbsent(
                    object.getClass(),
                    this::resolve
            );
        } else {
            resolvedType = Type.unknown();
        }

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Resolved object of java class {} to type {}", (object != null ? object.getClass() : null), resolvedType);
        }

        return resolvedType;
    }

    @NotNull
    public Type get(Class<?> clazz) {
        Type resolvedType;

        if (clazz != null) {
            resolvedType = types.computeIfAbsent(
                    clazz,
                    this::resolve
            );
        } else {
            resolvedType = Type.unknown();
        }

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Resolved object of java class {} to type {}", clazz, resolvedType);
        }

        return resolvedType;
    }

    private Type resolve(Class<?> clazz) {
        return resolve(TypeDescription.ForLoadedType.of(clazz).asGenericType());
    }

    public Type resolve(TypeDescription.Generic type) {
        try {
            Set<String> superTypes = getSuperTypes(type);

            return Type.builder()
                    .id(typeIdGenerator.incrementAndGet())
                    .name(trimGenerics(type.getActualName()))
                    .superTypeNames(superTypes)
                    .superTypeSimpleNames(superTypes.stream().map(ClassUtils::getSimpleNameFromName).collect(Collectors.toSet()))
                    .build();
        } catch (Throwable ex) {
            return Type.unknown();
        }
    }

    private Set<String> getSuperTypes(TypeDescription.Generic type) {
        Set<String> superTypes = new HashSet<>();
        try {

            TypeDefinition.Sort sort = type.getSort();
            TypeDescription.Generic superTypeToCheck = type;

            if (sort != TypeDefinition.Sort.VARIABLE && sort != TypeDefinition.Sort.VARIABLE_SYMBOLIC && sort != TypeDefinition.Sort.WILDCARD) {
                while (superTypeToCheck != null && !superTypeToCheck.equals(TypeDescription.Generic.OBJECT)) {

                    // do not add type name to super types
                    if (type != superTypeToCheck) {

                        String actualName = superTypeToCheck.asErasure().getActualName();
                        if (actualName.contains("$")) {
                            actualName = actualName.replace('$', '.');
                        }

                        superTypes.add(actualName);
                    }

                    for (TypeDescription.Generic interfface : superTypeToCheck.getInterfaces()) {
                        addInterfaceAndAllParentInterfaces(superTypes, interfface);
                    }

                    superTypeToCheck = superTypeToCheck.getSuperClass();
                }
            }
        } catch (Exception e) {
            // NOP
        }
        return superTypes;
    }

    private void addInterfaceAndAllParentInterfaces(Set<String> superTypes, TypeDescription.Generic interfface) {
        superTypes.add(prepareTypeName(interfface.asErasure().getActualName()));

        for (TypeDescription.Generic parentInterface : interfface.getInterfaces()) {
            addInterfaceAndAllParentInterfaces(superTypes, parentInterface);
        }
    }

    private String trimGenerics(String genericName) {
        int pos = genericName.indexOf('<');
        if (pos > 0) {
            genericName = genericName.substring(0, pos);
        }
        return genericName;
    }

    private String prepareTypeName(String genericName) {
        genericName = trimGenerics(genericName);

        if (genericName.contains("$")) {
            genericName = genericName.replace('$', '.');
        }
        return genericName;
    }

    @NotNull
    public Collection<Type> getAllResolved() {
        return types.values();
    }

    private static class InstanceHolder {
        private static final ByteBuddyTypeResolver context = new ByteBuddyTypeResolver();
    }
}
