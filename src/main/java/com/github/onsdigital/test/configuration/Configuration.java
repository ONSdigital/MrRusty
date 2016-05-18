package com.github.onsdigital.test.configuration;

import org.apache.commons.lang3.StringUtils;


public class Configuration {

    private static final String DEFAULT_FLORENCE_URL = "http://localhost:8081/florence/index.html";
    private static final String DEFAULT_SYSTEM_USER = "florence@magicroundabout.ons.gov.uk";
    private static final String DEFAULT_SYSTEM_PASSWORD = "master of puppets is";

    public static String getFlorenceUrl() {
        return StringUtils.defaultIfBlank(getValue("FLORENCE_URL"), DEFAULT_FLORENCE_URL);
    }

    public static String getSystemUsername() {
        return StringUtils.defaultIfBlank(getValue("FLORENCE_ADMIN_USER"), DEFAULT_SYSTEM_USER);
    }

    public static String getSystemUserPassword() {
        return StringUtils.defaultIfBlank(getValue("FLORENCE_ADMIN_PASSWORD"), DEFAULT_SYSTEM_PASSWORD);
    }


    public static String getBrowserStackUrl() {
        return getValue("BROWSERSTACK_URL");
    }

    /**
     * Gets a configured value for the given key from either the system
     * properties or an environment variable.
     * <p/>
     * Copied from {@link com.github.davidcarboni.restolino.Configuration}.
     *
     * @param key The name of the configuration value.
     * @return The system property corresponding to the given key (e.g.
     * -Dkey=value). If that is blank, the environment variable
     * corresponding to the given key (e.g. EXPORT key=value). If that
     * is blank, {@link StringUtils#EMPTY}.
     */
    static String getValue(String key) {
        return StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
    }
}
