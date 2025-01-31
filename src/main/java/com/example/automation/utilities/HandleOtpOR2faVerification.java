package com.example.automation.utilities;

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
        return waitUtils.waitForVisibilityShort(opt_or_2fa_page).isDisplayed();
    }

    /**
     * Handles OTP/2FA Verification.
     */
    public void handleOtp() throws InterruptedException {
        try {
            // Use longWait for visibility checks since the page load may take time
            try {
                if (isOtpPageDisplayed()) {
                    log.info("On OTP page");
                }
            } catch (TimeoutException ignored) {
                // OTP page not found, try checking for 2FA
                try {
                    if (is2FaPageDisplayed()) {
                        log.info("On 2FA page");
                    }
                } catch (TimeoutException ignoredAgain) {
                    log.warn("Neither OTP page nor 2FA page found.");
                    return; // Exit method if neither page is found
                }
            }

            // Enter the OTP or 2FA when the page is displayed
            String sampleOtp = "123456";
            for (int i = 0; i < sampleOtp.length(); i++) {
                WebElement pinInputField = waitUtils.waitForVisibilityShort(By.xpath("//input[@data-index='" + i + "']"));
                Thread.sleep(100);
                pinInputField.sendKeys(String.valueOf(sampleOtp.charAt(i)));
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

    /**
     * Checks whether OTP/2FA verification was successful.
     *
     * @return True if verification is successful, false otherwise.
     * @throws InterruptedException If thread sleep is interrupted.
     */
    private boolean isOtpVerifiedSuccessfully() throws InterruptedException {
        try {
            // Use shortWait for quick validation
            WebElement successIndicator = waitUtils.waitForVisibilityShort(is_signup_successful);

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
            } catch (TimeoutException ignored) {
                // No error message found
            }
        }
        return false;
    }
}
