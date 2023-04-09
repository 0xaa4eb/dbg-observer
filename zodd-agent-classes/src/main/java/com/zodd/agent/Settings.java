package com.zodd.agent;

import org.jetbrains.annotations.NotNull;

import com.zodd.agent.util.CommaSeparatedList;
import com.zodd.agent.util.MethodMatcherList;
import com.zodd.agent.util.PackageList;

/**
 * Agent settings which define what packages to instrument, at which method profiling should start, etc.
 * It's only possible to set settings via JMV system properties at the time.
 */
public class Settings {

    public static final String PACKAGES_PROPERTY = "zodd.packages";
    public static final String EXCLUDE_PACKAGES_PROPERTY = "zodd.exclude-packages";
    public static final String START_PROFILE_METHODS_PROPERTY = "zodd.methods";
    public static final String FILE_PATH_PROPERTY = "zodd.file";

    private final String file;
    @NotNull
    private final MethodMatcherList methodMatcherList;
    private final PackageList instrumentedPackages;
    private final PackageList excludedFromInstrumentationPackages;

    public Settings(
            String file,
            @NotNull MethodMatcherList methodMatcherList,
            PackageList instrumentedPackages,
            PackageList excludedFromInstrumentationPackages) {
        this.methodMatcherList = methodMatcherList;
        this.file = file;
        this.instrumentedPackages = instrumentedPackages;
        this.excludedFromInstrumentationPackages = excludedFromInstrumentationPackages;
    }

    public static Settings fromSystemProperties() {

        String methodsToProfile = System.getProperty(START_PROFILE_METHODS_PROPERTY, "");
        MethodMatcherList profilingStartMethods = MethodMatcherList.parse(methodsToProfile);
        String filePath = System.getProperty(FILE_PATH_PROPERTY);
        PackageList instrumentationPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(PACKAGES_PROPERTY, "")));
        PackageList excludedPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(EXCLUDE_PACKAGES_PROPERTY, "")));

        return new Settings(filePath, profilingStartMethods, instrumentationPackages, excludedPackages);
    }

    @NotNull
    public MethodMatcherList getProfileMethodList() {
        return methodMatcherList;
    }

    public String getFile() {
        return file;
    }

    public PackageList getInstrumentedPackages() {
        return instrumentedPackages;
    }

    public PackageList getExcludedFromInstrumentationPackages() {
        return excludedFromInstrumentationPackages;
    }
}
