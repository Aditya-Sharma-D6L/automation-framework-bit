package com.example.automation.tests.utilities;

import com.example.automation.config.Config;
import com.example.automation.tests.testdata.UserCredentials;

public class EnvironmentUtils {
    /**
     * Gets the email for the current environment from the UserCredentials data provider.
     *
     * @return The email for the current environment.
     */
    public static String getEmailForEnvironment() {
        String env = Config.getEnvironment(); // Get the current environment

        // Fetch credentials from UserCredentials data provider
        Object[][] allCredentials = UserCredentials.userCredentialsDataProvider();

        // Loop through the data and find the email for the current environment
        for (Object[] credentials : allCredentials) {
            if (credentials[0].equals(env)) { // Check if the environment matches
                return (String) credentials[1]; // Return the corresponding email
            }
        }

        // If no match is found, throw an exception
        throw new IllegalArgumentException("Unsupported environment or email not found for: " + env);
    }

    /**
     * Gets the password for the provided email in the current environment from the UserCredentials data provider.
     *
     * @param email The email address for which the password needs to be fetched.
     * @return The password corresponding to the provided email in the current environment.
     * @throws IllegalArgumentException If no matching email or environment is found.
     */
    public static String getPasswordForEmailInEnvironment(String email) {
        String env = Config.getEnvironment(); // Get the current environment

        // Fetch credentials from UserCredentials data provider
        Object[][] allCredentials = UserCredentials.userCredentialsDataProvider();

        // Loop through the data and find the password for the provided email in the current environment
        for (Object[] credentials : allCredentials) {
            if (credentials[0].equals(env) && credentials[1].equals(email)) { // Match environment and email
                return (String) credentials[2]; // Return the corresponding password
            }
        }

        // If no match is found, throw an exception
        throw new IllegalArgumentException("No password found for email: " + email + " in environment: " + env);
    }

}
