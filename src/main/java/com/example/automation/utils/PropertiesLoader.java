package com.example.automation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static final String DEFAULT_PROPERTIES_FILE = "application.properties";
    private static final Properties properties = new Properties();
    private static String activeEnvironment;

    static {
        // Load the default properties file first
        loadProperties(DEFAULT_PROPERTIES_FILE);
    }

    /**
     * Load properties from a file on the classpath.
     * Supports environment-specific overrides (e.g., "application-qa.properties").
     */
    public static void loadProperties(String fileName) {
        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("Properties file not found: " + fileName);
            }
            properties.clear();
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + fileName, e);
        }
    }

    /**
     * Set the active environment (e.g., "qa", "prod").
     * Automatically loads environment-specific properties if they exist.
     */
    public static void setEnvironment(String env) {
        activeEnvironment = env;
        // Load environment-specific properties if available
        if (env != null && !env.isEmpty()) {
            String envFileName = "application-" + env + ".properties";
            loadProperties(envFileName);
        }
    }

    /**
     * Get a property value with environment-aware fallback.
     */
    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in "
                    + (activeEnvironment != null ? "environment '" + activeEnvironment + "'" : "default properties"));
        }
        return value;
    }

    /**
     * Get a property with a default value if missing.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
