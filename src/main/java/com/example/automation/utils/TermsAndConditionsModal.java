package com.example.automation.utils;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;

@Slf4j
public class TermsAndConditionsModal {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    // Locators for TnC popup (could appear as a modal or overlay)
    private static final By checkBox            = By.xpath("//label/span/p[text()='I agree to the BitDelta Terms and conditions']");
    private static final By scrollButton        = By.xpath("//div[contains(text(),'Scroll Down')]");
    private static final By agreeButton         = By.xpath("//button[normalize-space()='Agree']");
    private static final By verify_tnc_accepted = By.xpath("//h2[text()='Verify OTP'] | " +
            "//h2[text()='Two Factor Verification'] | " +
            "//div[contains(@class,'geetest_btn')] | " +
            "(//button[@type='button' and text()='Register'])[1]" +
            "//button[@type='button' and text()='Register'][2]");


    /**
     * Constructor for TermsAndConditionsModal.
     * @param driver WebDriver instance.
     * @param waitUtils WaitUtils instance.
     */
    public TermsAndConditionsModal(WebDriver driver, WaitUtils waitUtils) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver, 10, 3);
    }

    /**
     * Accepts the Terms & Conditions (TnC) popup.
     */
    public void acceptTermsAndConditionsPopup() {
        try {
            log.info("Attempting to accept Terms and Conditions...");

            // Wait for and click the checkbox to agree to the terms
            WebElement checkboxElement = waitUtils.waitForVisibilityLong(checkBox);
            if (checkboxElement.isDisplayed() && checkboxElement.isEnabled()) {
                checkboxElement.click();
                log.info("Checked 'I agree to Terms and Conditions'");
            } else {
                log.warn("Checkbox is not visible or not enabled.");
                return;
            }

            // Wait for and click the "Scroll Down" button (if present)
            try {
                WebElement scrollButtonElement = waitUtils.waitForVisibilityShort(scrollButton);
                if (scrollButtonElement.isDisplayed()) {
                    log.info("Clicking 'Scroll Down' button...");
                    Retry.retryOperation(() -> {
                        scrollButtonElement.click();
                        return null;
                    }, 2, 1000, "Clicking Scroll Down button");
                }
            } catch (Exception e) {
                log.warn("Scroll Down button not found or not clickable. Skipping...");
            }

            // Ensure "Agree" button is clickable and click it
            WebElement agreeButtonElement = waitUtils.waitForClickabilityLong(agreeButton);
            if (agreeButtonElement.isDisplayed() && agreeButtonElement.isEnabled()) {
                agreeButtonElement.click();
                log.info("Clicked 'Agree' button.");
            } else {
                log.error("Agree button is not visible or not clickable.");
                return;
            }

            // Verify if the TnC modal is accepted and the next page is loaded
            if (waitUtils.waitForInvisibilityShort(checkBox) || waitUtils.waitForVisibilityLong(verify_tnc_accepted).isDisplayed()) {
                log.info("TnC accepted successfully, proceeding to the next step.");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.error("Failed to accept Terms and Conditions popup.");
        }
    }
}
