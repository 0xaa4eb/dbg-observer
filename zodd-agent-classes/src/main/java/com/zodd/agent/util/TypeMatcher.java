package com.zodd.agent.util;

import com.zodd.agent.Type;

public interface TypeMatcher {

    String WILDCARD = "*";
    String DOUBLE_WILDCARD = "**";

    static TypeMatcher parse(String text) {
        if (text.equals(WILDCARD) || text.equals(DOUBLE_WILDCARD)) {
            return new AcceptAllTypeMatcher();
        } else {
            return AntPatternTypeMatcher.of(text);
        }
    }

    boolean matchesSuperTypes();

    boolean matches(Type type);

    class AcceptAllTypeMatcher implements TypeMatcher {

        @Override
        public boolean matchesSuperTypes() {
            return false;
        }

        @Override
        public boolean matches(Type type) {
            return true;
        }

        public String toString() {
            return WILDCARD;
        }
    }

    class AntPatternTypeMatcher implements TypeMatcher {

        private final String patternText;
        private final String rawPatternText;
        private final boolean matchSuperTypes;
        private final AntPathMatcher antMatcher;

        public AntPatternTypeMatcher(String patternText, boolean matchSuperTypes) {
            this.antMatcher = new AntPathMatcher(".");
            this.rawPatternText = patternText;
            this.matchSuperTypes = matchSuperTypes;
            this.patternText = patternText.replace('$', '.');
        }

        static TypeMatcher of(String patternText) {
            patternText = patternText.replace('$', '.');
            boolean matchSuperTypes = false;

            if (patternText.startsWith("^")) {
                matchSuperTypes = true;
                patternText = patternText.substring(1);
            }

            if (patternText.startsWith("**.") && patternText.lastIndexOf('.') == 2) {
                // formats like **.ABC can be matched by simple class name type matchers
                return new SimpleNameTypeMatcher(patternText.substring(3), matchSuperTypes);
            } else {
                return new AntPatternTypeMatcher(patternText, matchSuperTypes);
            }
        }

        @Override
        public boolean matchesSuperTypes() {
            return matchSuperTypes;
        }

        @Override
        public boolean matches(Type type) {
            String nameToCheckAgainst = type.getName();
            if (type.getName().contains("$")) {
                nameToCheckAgainst = nameToCheckAgainst.replace('$', '.');
            }
            if (antMatcher.match(patternText, nameToCheckAgainst)) {
                return true;
            }

            if (matchSuperTypes) {
                for (String superType : type.getSuperTypeNames()) {
                    if (antMatcher.match(patternText, superType)) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public String toString() {
            return rawPatternText;
        }
    }

    class SimpleNameTypeMatcher implements TypeMatcher {

        private final String simpleName;
        private final boolean matchSuperTypes;

        public SimpleNameTypeMatcher(String simpleName, boolean matchSuperTypes) {
            this.simpleName = simpleName;
            this.matchSuperTypes = matchSuperTypes;
        }

        @Override
        public boolean matchesSuperTypes() {
            return matchSuperTypes;
        }

        @Override
        public boolean matches(Type type) {
            String simpleName = ClassUtils.getSimpleNameFromName(type.getName());
            if (this.simpleName.equalsIgnoreCase(simpleName)) {
                return true;
            }

            for (String superType : type.getSuperTypeNames()) {
                if (this.simpleName.equalsIgnoreCase(ClassUtils.getSimpleNameFromName(superType))) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String toString() {
            return "**." + simpleName;
        }
    }
}
