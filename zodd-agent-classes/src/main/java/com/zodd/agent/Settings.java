package com.zodd.agent;

import org.jetbrains.annotations.NotNull;

/**
 * Agent settings which define what packages to instrument, at which method recording should start, etc.
 * It's only possible to set settings via JMV system properties at the time.
 */
public class Settings {

    public static final String PACKAGES_PROPERTY = "ulyp.packages";
    public static final String EXCLUDE_PACKAGES_PROPERTY = "ulyp.exclude-packages";
    public static final String START_RECORDING_METHODS_PROPERTY = "ulyp.methods";
    public static final String FILE_PATH_PROPERTY = "ulyp.file";

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

        String methodsToRecord = System.getProperty(START_RECORDING_METHODS_PROPERTY, "");
        MethodMatcherList recordingStartMethods = MethodMatcherList.parse(methodsToRecord);
        String filePath = System.getProperty(FILE_PATH_PROPERTY);
        PackageList instrumentationPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(PACKAGES_PROPERTY, "")));
        PackageList excludedPackages = new PackageList(CommaSeparatedList.parse(System.getProperty(EXCLUDE_PACKAGES_PROPERTY, "")));

        return new Settings(filePath, recordingStartMethods, instrumentationPackages, excludedPackages);
    }

    @NotNull
    public MethodMatcherList getRecordMethodList() {
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
