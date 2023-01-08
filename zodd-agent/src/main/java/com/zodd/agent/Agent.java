package com.zodd.agent;

import java.lang.instrument.Instrumentation;

import com.zodd.agent.util.ByteBuddyMethodResolver;
import com.zodd.agent.util.ErrorLoggingInstrumentationListener;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * The agent entry point which is invoked by JVM itself
 */
public class Agent {

    private static final String ULYP_LOGO =
            "   __  __    __ __  __    ____ \n" +
                    "  / / / /   / / \\ \\/ /   / __ \\\n" +
                    " / / / /   / /   \\  /   / /_/ /\n" +
                    "/ /_/ /   / /___ / /   / ____/ \n" +
                    "\\____/   /_____//_/   /_/      \n" +
                    "                               ";

    public static void start(String args, Instrumentation instrumentation) {

        // Touch first and initialize shadowed slf4j
        String logLevel = LoggingSettings.getLoggingLevel();

        if (AgentContext.isLoaded()) {
            return;
        }
        AgentContext.setLoaded();

        AgentContext instance = AgentContext.getInstance();
        Settings settings = Settings.fromSystemProperties();

        PackageList instrumentedPackages = settings.getInstrumentedPackages();
        PackageList excludedPackages = settings.getExcludedFromInstrumentationPackages();
        MethodMatcherList methods = settings.getRecordMethodList();

        System.out.println(ULYP_LOGO);
        System.out.println("ULYP agent started, logging level = " + logLevel + ", settings: " + settings);

        ElementMatcher.Junction<TypeDescription> instrumentationMatcher = null;

        for (String packageToInstrument : instrumentedPackages) {
            if (instrumentationMatcher == null) {
                instrumentationMatcher = ElementMatchers.nameStartsWith(packageToInstrument);
            } else {
                instrumentationMatcher = instrumentationMatcher.or(ElementMatchers.nameStartsWith(packageToInstrument));
            }
        }

        excludedPackages.add("java");
        excludedPackages.add("javax");
        excludedPackages.add("jdk");
        excludedPackages.add("sun");

        for (String excludedPackage : excludedPackages) {
            if (instrumentationMatcher == null) {
                instrumentationMatcher = ElementMatchers.not(ElementMatchers.nameStartsWith(excludedPackage));
            } else {
                instrumentationMatcher = instrumentationMatcher.and(ElementMatchers.not(ElementMatchers.nameStartsWith(excludedPackage)));
            }
        }

        ElementMatcher.Junction<TypeDescription> finalTypeMatcher = ElementMatchers
                .not(ElementMatchers.nameStartsWith("com.zodd"))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("shadowed")));

        if (instrumentationMatcher != null) {
            finalTypeMatcher = finalTypeMatcher.and(instrumentationMatcher);
        }

        ByteBuddyMethodResolver resolver = new ByteBuddyMethodResolver();
        MethodIdFactory methodIdFactory = new MethodIdFactory(resolver);

        AgentBuilder.Identified.Extendable agentBuilder = new AgentBuilder.Default()
                .type(finalTypeMatcher)
                .transform((builder, typeDescription, classLoader, module) -> builder.visit(
                        Advice.withCustomMapping()
                                .bind(methodIdFactory)
                                .to(MethodCallRecordingAdvice.class)
                                .on(ElementMatchers
                                    .isMethod()
                                    .and(ElementMatchers.not(ElementMatchers.isAbstract()))
                                    .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                    .and(methodDescription -> methods.anyMatch(resolver.resolve(methodDescription)))
                                )
                ));

        AgentBuilder agent = agentBuilder.with(AgentBuilder.TypeStrategy.Default.REDEFINE);

        if (LoggingSettings.TRACE_ENABLED) {
            agent = agent.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
        } else {
            agent = agent.with(new ErrorLoggingInstrumentationListener());
        }

        agent.installOn(instrumentation);
    }
}
