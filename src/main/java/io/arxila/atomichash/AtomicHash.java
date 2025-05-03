/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2025 Arxila OSS (https://arxila.io)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 *
 * =========================================================================
 */
package io.arxila.atomichash;

import java.io.InputStream;
import java.util.Properties;

/**
 * Class meant to keep some constants related to the version of the AtomicHash library being used, build date, etc.
 * <p>
 * This can be used in conditional code that requires a specific version of the library to be present.
 */
public final class AtomicHash {

    public static final String VERSION;
    public static final String BUILD_TIMESTAMP;

    public static final int VERSION_MAJOR;
    public static final int VERSION_MINOR;
    public static final int VERSION_BUILD;
    public static final String VERSION_TYPE;

    private static final String VERSION_TYPE_RELEASE = "RELEASE";
    private static final String RESOURCE_NAME = "io/arxila/atomichash/atomichash.properties";


    static {

        String version = null;
        String buildTimestamp = null;
        try {
            final Properties properties = new Properties();
            properties.load(findResourceAsStream());
            version = properties.getProperty("version");
            buildTimestamp = properties.getProperty("build.date");
        } catch (final Exception ignored) {
            // Ignored: we don't have such information, might be due to IDE configuration
        }

        VERSION = version;
        BUILD_TIMESTAMP = buildTimestamp;

        if (VERSION == null || VERSION.trim().isEmpty()) {

            VERSION_MAJOR = 0;
            VERSION_MINOR = 0;
            VERSION_BUILD = 0;
            VERSION_TYPE = "UNKNOWN";

        } else {

            try {

                String versionRemainder = VERSION;

                int separatorIdx = versionRemainder.indexOf('.');
                VERSION_MAJOR = Integer.parseInt(versionRemainder.substring(0,separatorIdx));
                versionRemainder = versionRemainder.substring(separatorIdx + 1);

                separatorIdx = versionRemainder.indexOf('.');
                VERSION_MINOR = Integer.parseInt(versionRemainder.substring(0, separatorIdx));
                versionRemainder = versionRemainder.substring(separatorIdx + 1);

                separatorIdx = versionRemainder.indexOf('.');
                if (separatorIdx < 0) {
                    separatorIdx = versionRemainder.indexOf('-');
                }
                if (separatorIdx >= 0) {
                    VERSION_BUILD = Integer.parseInt(versionRemainder.substring(0, separatorIdx));
                    VERSION_TYPE = versionRemainder.substring(separatorIdx + 1);
                } else {
                    VERSION_BUILD = Integer.parseInt(versionRemainder);
                    VERSION_TYPE = VERSION_TYPE_RELEASE;
                }

            } catch (final Exception e) {
                throw new ExceptionInInitializerError(
                        "Exception during initialization of atomichash versioning utilities. " +
                            "Identified atomichash version is '" + VERSION + "', which does not follow " +
                            "the {major}.{minor}.{build}[-SNAPSHOT] format.");
            }

        }

    }


    public static boolean isVersionStableRelease() {
        return VERSION_TYPE_RELEASE.equals(VERSION_TYPE);
    }


    private static InputStream findResourceAsStream() {

        // First, try the context class loader
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            final InputStream inputStream = contextClassLoader.getResourceAsStream(AtomicHash.RESOURCE_NAME);
            if (inputStream != null) {
                return inputStream;
            }
        }

        // Second, try the class loader that loaded this class
        final ClassLoader classClassLoader = AtomicHash.class.getClassLoader();
        return (classClassLoader != null) ? classClassLoader.getResourceAsStream(AtomicHash.RESOURCE_NAME) : null;

    }




    private AtomicHash() {
        super();
    }

}
