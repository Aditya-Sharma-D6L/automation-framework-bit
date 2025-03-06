package com.example.automation.utils;

import com.example.automation.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class HandleOtpOR2faVerification {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    private static final By verify_otp_heading   = By.xpath("//h2[text()='Verify OTP']");
    private static final By verify_2fa_heading   = By.xpath("//h2[text()='Two Factor Verification']");
    private static final By opt_or_2fa_page      = By.xpath("//h2[text()='Verify OTP'] | " +
                                                    "//h2[text()='Two Factor Verification'] | " +
                                                    "//label/span/p[text()='I agree to the BitDelta Terms and conditions']");
    private static final By is_signup_successful = By.xpath("//div/p[text()='Wallet'] | " +
                                                    "//h2[text()='Individual Questionnaire'] | " +
                                                    "//h1[text()='Please provide the information'] | " +
                                                    "//p[text()='Complete identity verification to unlock all features. ']");
    private static final By error_message_on_otp_page = By.id("field-:r8:-feedback");

    /**
     * Constructor to initialize the driver and wait utility.
     *
     * @param driver   The WebDriver instance.
     * @param waitUtils The WaitUtils instance for handling waits.
     */
    public HandleOtpOR2faVerification(WebDriver driver, WaitUtils waitUtils) {
        this.driver = driver;
        this.waitUtils = waitUtils;
    }

    /**
     * Checks if the OTP page is displayed or not
     * @return boolean value
     */
    public boolean isOtpPageDisplayed() {
        return waitUtils.waitForVisibilityLong(verify_otp_heading).isDisplayed();
    }

    /**
     * Checks if the 2FA page is displayed or not
     * @return boolean value
     */
    public boolean is2FaPageDisplayed() {
        return waitUtils.waitForVisibilityLong(verify_2fa_heading).isDisplayed();
    }

    /**
     * Checks if either OTP or 2FA page is displayed or not
     * @return boolean value
     */
    public boolean isOtpOr2FaPageDisplayed() {
        try {
            return waitUtils.waitForVisibilityShort(opt_or_2fa_page).isDisplayed(); // Returns true if element is found
        } catch (TimeoutException e) {
            return false;  // Return false if the element is NOT found (normal case)
        } catch (Exception e) {
            log.error("Unexpected error while checking for OTP/2FA page visibility: {}", e.getMessage());
            return false;  // Log other unexpected errors
        }
    }

    /**
     * Handles OTP/2FA Verification.
     */
    public void handleOtp() {
        try {
            // Check if the OTP page or 2FA page is displayed
            try {
                if (isOtpPageDisplayed()) {
                    log.info("On OTP page");
                }
            } catch (Exception e) {
                try {
                    if (is2FaPageDisplayed()) {
                        log.info("On 2FA page");
                    }
                } catch (Exception ex) {
                    log.warn("Neither OTP page nor 2FA page found.");
                    return; // Exit method if neither page is found
                }
            }

            // Determine OTP based on environment
            String otp;
            String activeProfile = System.getProperty("spring.profiles.active");
            if (activeProfile.equals("prod")) {
                log.info("Running in PROD environment - Generating OTP using OTPUtil.");
                String username = "copt1@yopmail.com"; // Replace with dynamic user retrieval if needed
                otp = OTPUtil.generateTOTPForUser(username);
            } else {
                log.info("Non-PROD environment detected, using sample OTP.");
                otp = "123456"; // Use dummy OTP for non-prod environments
            }

            // Enter the OTP or 2FA code
            for (int i = 0; i < otp.length(); i++) {
                WebElement pinInputField = waitUtils.waitForVisibilityShort(By.xpath("//input[@data-index='" + i + "']"));
                pinInputField.sendKeys(String.valueOf(otp.charAt(i)));
                waitForSeconds(1);
            }

            // Click the "Verify" button and validate the verification result
            boolean isVerified = isOtpVerifiedSuccessfully();
            if (isVerified) {
                log.info("OTP/2FA Verified Successfully!");
            } else {
                log.error("OTP/2FA Verification Failed!");
            }

        } catch (TimeoutException e) {
            log.error("OTP/2FA verification page did not load.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted: {}", e.getMessage());
        }
    }

    public void waitForSeconds(int timeInSeconds) throws InterruptedException {
        Thread.sleep(timeInSeconds * 1000L);
    }

    /**
     * Checks whether OTP/2FA verification was successful.
     *
     * @return True if verification is successful, false otherwise.
     * @throws InterruptedException If thread sleep is interrupted.
     */
    private boolean isOtpVerifiedSuccessfully() throws InterruptedException {
        try {
            // Use shortWait for quick validation
            WebElement successIndicator = waitUtils.waitForVisibilityLong(is_signup_successful);

            Retry.retryOperation(() -> {
                boolean flag = successIndicator.isDisplayed();
                return flag ? true : null; // Return null since click doesn't return anything
            }, 3, 1000, "Click accept button on OTP/2FA page");

            return successIndicator.isDisplayed();
        } catch (Exception e) {
            // If no success indicator, check for an error message
            try {
                WebElement errorOtpRequired = waitUtils.waitForVisibilityShort(error_message_on_otp_page);
                if (errorOtpRequired.isDisplayed()) {
                    log.error("Error: OTP verification failed");
                    return false;
                }
            } catch (RuntimeException e1) {
                // No error message found
            }
        }
        return false;
    }
}
