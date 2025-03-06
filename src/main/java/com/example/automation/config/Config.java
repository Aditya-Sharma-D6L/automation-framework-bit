package com.example.automation.config;

import com.example.automation.utils.PropertiesLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Config {

    private static final ThreadLocal<String> ENV_THREAD_LOCAL = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, String> BASE_URLS = new ConcurrentHashMap<>();
    private static final Properties properties = new Properties();  // Define properties object

    static {
        // Preload all environments at startup
        loadProperties("application.properties"); // Load the default properties first
    }

    /**
     * Sets the environment and loads the corresponding environment-specific properties.
     */
    public static void setEnvironment() {
        // Retrieve the active Spring profile directly from the system property
        String env = System.getProperty("spring.profiles.active");

        if (env == null || env.isEmpty()) {
            throw new IllegalArgumentException("Environment cannot be null or empty");
        }

        // Set the environment in the ThreadLocal variable
        ENV_THREAD_LOCAL.set(env.toLowerCase());

        // Load the corresponding environment-specific properties file
        String propertiesFile = "application-" + env.toLowerCase() + ".properties"; // Assuming environment-specific properties
        loadProperties(propertiesFile); // Load the environment-specific properties file

        // Log the environment set for debugging purposes
        System.out.println("Environment set to: " + env);
    }

    /**
     * Returns the current environment.
     * @return The current environment.
     */
    public static String getEnvironment() {
        String env = ENV_THREAD_LOCAL.get();
        if (env == null) {
            throw new IllegalStateException("Environment not set. Call setEnvironment() first.");
        }
        return env;
    }

    /**
     * Returns the base URL for the current environment.
     * @return The base URL for the environment.
     */
    public static String getBaseUrl() {
        String env = ENV_THREAD_LOCAL.get();
        if (env == null) {
            throw new IllegalStateException("Environment not set. Call setEnvironment() first.");
        }
        return BASE_URLS.getOrDefault(env, throwUrlNotFoundError(env));
    }

    /**
     * Loads properties from a specified file.
     * @param fileName The name of the properties file to load.
     */
    private static void loadProperties(String fileName) {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("Properties file not found: " + fileName);
            }
            properties.clear();
            properties.load(input); // Load the properties into the Properties object
            System.out.println("Loaded properties file: " + fileName);

            // If the properties file is environment-specific, populate BASE_URLS
            if (fileName.contains("application-")) {
                String env = fileName.substring(fileName.lastIndexOf('-') + 1, fileName.indexOf(".properties"));
                String baseUrl = properties.getProperty("baseUrl");
                System.out.println("Loaded base URL for " + env + ": " + baseUrl);  // Log the loaded base URL
                if (baseUrl == null) {
                    throw new RuntimeException("Base URL not found for environment: " + env);
                }
                BASE_URLS.put(env, baseUrl);
                System.out.println("BASE_URL for " + env + ": " + BASE_URLS.get(env));  // Log the BASE_URLS map

            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + fileName, e);
        }
    }

    /**
     * Returns the browser from the properties.
     * @return The browser type.
     */
    public static String getBrowser() {
        return PropertiesLoader.getProperty("browser");
    }

    /**
     * Throws an error if the base URL is not found for a given environment.
     * @param env The environment.
     * @return Throws a RuntimeException.
     */
    private static String throwUrlNotFoundError(String env) {
        throw new RuntimeException("Base URL not found for environment: " + env);
    }
}
