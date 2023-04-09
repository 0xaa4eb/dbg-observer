package com.zodd.agent;

import java.lang.instrument.Instrumentation;
import java.util.Optional;

import com.zodd.agent.util.*;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * The agent entry point which is invoked by JVM itself
 */
public class Agent {

    public static void start(String args, Instrumentation instrumentation) {

        // Touch first and initialize shadowed slf4j
        String logLevel = LoggingSettings.getLoggingLevel();
        Settings settings = Settings.fromSystemProperties();
        if (settings.isAgentDisabled()) {
            return;
        }

        if (AgentContext.isLoaded()) {
            return;
        }
        AgentContext.initInstance(settings);
        AgentContext context = AgentContext.getInstance();

        System.out.println("Zodd agent started, logging level = " + logLevel + ", settings: " + settings);

        MethodMatcherList profileMethodList = settings.getProfileMethodList();
        MethodIdFactory methodIdFactory = new MethodIdFactory(context.getMethodRepository(), profileMethodList);

        ElementMatcher.Junction<TypeDescription> ignoreMatcher = buildIgnoreMatcher(settings);
        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = buildInstrumentationMatcher(settings);

        AgentBuilder.Identified.Extendable agentBuilder = new AgentBuilder.Default()
                .ignore(ignoreMatcher)
                .type(instrumentationMatcher)
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(
                        Advice.withCustomMapping()
                                .bind(methodIdFactory)
                                .to(WallTimeProfileAdvice.class)
                                .on(buildMethodsMatcher(settings))
                ));

        AgentBuilder agent = agentBuilder.with(AgentBuilder.TypeStrategy.Default.REDEFINE);

        if (LoggingSettings.TRACE_ENABLED) {
            agent = agent.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
        } else {
            agent = agent.with(new ErrorLoggingInstrumentationListener());
        }

        agent.installOn(instrumentation);
    }

    private static ElementMatcher.Junction<MethodDescription> buildMethodsMatcher(Settings settings) {
        MethodMatcherList profileMethods = settings.getProfileMethodList();
        ByteBuddyMethodResolver byteBuddyMethodResolver = new ByteBuddyMethodResolver(
                profileMethods.useSuperTypes() ? ByteBuddyTypeConverter.SUPER_TYPE_DERIVING_INSTANCE : ByteBuddyTypeConverter.INSTANCE
        );
        return ElementMatchers.isMethod()
                .and(ElementMatchers.not(ElementMatchers.isAbstract()))
                .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                .and(methodDescription -> profileMethods.anyMatch(byteBuddyMethodResolver.resolve(methodDescription)));
    }

    private static ElementMatcher.Junction<TypeDescription> buildInstrumentationMatcher(Settings settings) {
        PackageList instrumentatedPackages = settings.getInstrumentedPackages();
        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = null;

        for (String packageToInstrument : instrumentatedPackages) {
            if (instrumentationMatcher == null) {
                instrumentationMatcher = ElementMatchers.nameStartsWith(packageToInstrument);
            } else {
                instrumentationMatcher = instrumentationMatcher.or(ElementMatchers.nameStartsWith(packageToInstrument));
            }
        }

        return Optional.ofNullable(instrumentationMatcher).orElse(ElementMatchers.any());
    }

    private static ElementMatcher.Junction<TypeDescription> buildIgnoreMatcher(Settings settings) {
        PackageList excludedPackages = settings.getExcludedFromInstrumentationPackages();

        ElementMatcher.Junction<TypeDescription> ignoreMatcher = ElementMatchers.nameStartsWith("java.")
                .or(ElementMatchers.nameStartsWith("javax."))
                .or(ElementMatchers.nameStartsWith("jdk."))
                .or(ElementMatchers.nameStartsWith("sun"))
                .or(ElementMatchers.nameStartsWith("shadowed"))
                .or(ElementMatchers.nameStartsWith("com.sun"))
                .or(ElementMatchers.nameStartsWith("com.zodd"));

        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = buildInstrumentationMatcher(settings);
        if (instrumentationMatcher != ElementMatchers.<TypeDescription>any()) {
            ignoreMatcher = ElementMatchers.not(instrumentationMatcher).and(ignoreMatcher);
        }

        for (String excludedPackage : excludedPackages) {
            ignoreMatcher = ignoreMatcher.or(ElementMatchers.nameStartsWith(excludedPackage));
        }

        return ignoreMatcher;
    }
}
