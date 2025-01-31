package com.example.automation.tests.testdata;

import org.testng.annotations.DataProvider;

public class PasswordValidationData {

    @DataProvider(name = "passwordValidationDataProvider")
    public static Object[][] passwordValidationDataProvider() {
        return new Object[][]{
                {"Pass12345", "Should contain at least one special character"},
                {"PASS@123", "Should contain at least one lowercase letter"},
                {"pass@123", "Should contain at least one uppercase letter"},
                {"Pass@word", "Should contain at least one number"},
                {"Pass 12345@", "Should not contain any spaces"},
                {"Pass@123456789012", "Should not contain more than 17 characters"},
                {"<script></script>", "Should not accept values with <script>"}
        };
    }
}
