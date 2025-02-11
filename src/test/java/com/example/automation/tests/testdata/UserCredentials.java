package com.example.automation.tests.testdata;

import com.example.automation.config.Config;
import org.testng.annotations.DataProvider;

import java.util.Arrays;

public class UserCredentials {

    @DataProvider(name = "userCredentials", parallel = true)
    public static Object[][] userCredentialsDataProvider() {
        // All environment-specific credentials
        Object[][] allCredentials = {
                {"qa", "copt1@yopmail.com", "Pass@123456"},
                {"staging", "mtaditya2@yopmail.com", "Pass@12345"},
                {"eks", "mtaditya2@yopmail.com", "Pass@12345"},
                {"prod", "aditya.sharma@delta6labs.com", "Pass@12345"}
        };

        // Get the current environment from the configuration
        String currentEnvironment = Config.getEnvironment();

        // Filter credentials to return only for the active environment
        return Arrays.stream(allCredentials)
                .filter(data -> data[0].equals(currentEnvironment))
                .toArray(Object[][]::new);
    }
}
