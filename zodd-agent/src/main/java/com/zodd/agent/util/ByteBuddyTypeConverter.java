package com.zodd.agent.util;

import com.zodd.agent.Type;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class ByteBuddyTypeConverter {

    public static final ByteBuddyTypeConverter INSTANCE = new ByteBuddyTypeConverter(false);
    public static final ByteBuddyTypeConverter SUPER_TYPE_DERIVING_INSTANCE = new ByteBuddyTypeConverter(true);

    private static final TypeDescription.Generic BYTE_BUDDY_STRING_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(String.class);

    private static final TypeDescription.Generic PRIMITIVE_LONG = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(long.class);
    private static final TypeDescription.Generic PRIMITIVE_INT = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(int.class);
    private static final TypeDescription.Generic PRIMITIVE_SHORT = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(short.class);
    private static final TypeDescription.Generic PRIMITIVE_BYTE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(byte.class);
    private static final Set<TypeDescription.Generic> PRIMITIVE_INTEGRAL_TYPES = new HashSet<>();
    private static final Set<TypeDescription.Generic> BOXED_INTEGRAL_TYPES = new HashSet<>();
    private static final Set<TypeDescription.Generic> PRIMITIVE_DOUBLE_TYPES = new HashSet<>();
    private static final TypeDescription.Generic PRIMITIVE_CHAR_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(char.class);
    private static final TypeDescription.Generic BOXED_CHAR_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Character.class);
    private static final TypeDescription CLASS_OBJECT_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Class.class).asErasure();
    private static final TypeDescription COLLECTION_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Collection.class).asErasure();
    private static final TypeDescription MAP_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Map.class).asErasure();
    private static final TypeDescription THROWABLE_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Throwable.class).asErasure();
    private static final TypeDescription NUMBER_TYPE = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Number.class).asErasure();
    private static final TypeDescription.Generic PRIMITIVE_BOOLEAN = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(boolean.class);
    private static final TypeDescription.Generic BOXED_BOOLEAN = TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Boolean.class);
    private static final AtomicLong typeIdGenerator = new AtomicLong(0L);

    static {
        PRIMITIVE_INTEGRAL_TYPES.add(PRIMITIVE_LONG);
        PRIMITIVE_INTEGRAL_TYPES.add(PRIMITIVE_INT);
        PRIMITIVE_INTEGRAL_TYPES.add(PRIMITIVE_SHORT);
        PRIMITIVE_INTEGRAL_TYPES.add(PRIMITIVE_BYTE);

        PRIMITIVE_DOUBLE_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(double.class));
        PRIMITIVE_DOUBLE_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(float.class));

        BOXED_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Long.class));
        BOXED_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Integer.class));
        BOXED_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Short.class));
        BOXED_INTEGRAL_TYPES.add(TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(Byte.class));
    }

    private final boolean deriveSuperTypes;

    public ByteBuddyTypeConverter(boolean deriveSuperTypes) {
        this.deriveSuperTypes = deriveSuperTypes;
    }

    public Type convert(TypeDescription.Generic type) {
        try {

            Type.TypeBuilder typeBuilder = Type.builder()
                .id(typeIdGenerator.incrementAndGet())
                .name(trimGenerics(type.getActualName()));

            if (deriveSuperTypes) {
                typeBuilder.superTypeNames(deriveSuperTypes(type));
            }

            return typeBuilder.build();
        } catch (Throwable ex) {
            return Type.unknown();
        }
    }

    private Set<String> deriveSuperTypes(TypeDescription.Generic type) {
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
}
