package com.example.automation.utilities;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

@Slf4j
public class Captcha {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    /**
     * Constructor for Captcha solver.
     *
     * @param driver WebDriver instance
     * @param waitUtils WaitUtils instance for handling waits
     */
    public Captcha(WebDriver driver, WaitUtils waitUtils) {
        this.driver = driver;
        this.waitUtils = waitUtils;
    }

    //locators
    private static final By sliderPart          = By.xpath("//div[contains(@class,'geetest_btn')]");
    private static final By successMessages = By.xpath(
            "//p[contains(text(), 'Please enter correct credentials')] | " +
                    "//h2[contains(text(),'Verify OTP')] | " +
                    "//h2[text()='Two Factor Verification'] | " +
                    "//p[text()='Request successfully completed'] | " +
                    "//h2[text()='Individual Questionnaire'] | " +
                    "//h1//p[text()='Every Trade Counts with'] | " +
                    "//p[normalize-space()='Your asset safety, Our responsibility']"
    );
    private static final By geetest_popup_ghost = By.xpath("//div[contains(@class,'geetest_popup_ghost')]");

    /**
     * Solves the GeeTest Captcha by interacting with the slider.
     */
    public void solveGeetestCaptcha(WebDriver driver) {
        while (true) { // Infinite loop until captcha is solved
            try {
                // Wait for the slider to be clickable
                WebElement slider = waitUtils.waitForClickabilityLong(sliderPart);

                // Perform sliding action
                Actions actions = new Actions(this.driver);
                actions.clickAndHold(slider)
                        .moveByOffset(202, 0) // Adjust this dynamically
                        .release()
                        .perform();

                // Wait for a brief period to allow captcha validation
                Thread.sleep(1500);

                // Check if captcha is solved
                if (isCaptchaSolved()) {
                    log.info("Captcha solved successfully!");
                    break; // Exit the loop if solved
                } else {
                    log.warn("Captcha validation failed, retrying...");
                }
            } catch (ElementClickInterceptedException e) {
                log.warn("Overlay detected, waiting...");
                waitForOverlayToDisappear();
            } catch (Exception e) {
                //
            }
        }
    }

    /**
     * Checks if the captcha has been successfully solved.
     *
     * @return true if captcha is solved, otherwise false.
     */
    private boolean isCaptchaSolved() throws InterruptedException {
        // Implement logic to check if captcha is solved
        // Example: Look for an element or message that indicates success
        try {
            WebElement successMessage = waitUtils.waitForVisibilityLong(successMessages);
            Thread.sleep(2500);
            return successMessage.isDisplayed();
        } catch (NoSuchElementException | TimeoutException e) {
            return false;
        }
    }

    /**
     * Waits for the captcha overlay to disappear before retrying.
     */
    private void waitForOverlayToDisappear() {
        try {
            waitUtils.waitForVisibilityShort(geetest_popup_ghost);
        } catch (TimeoutException e) {
            System.out.println("Overlay did not disappear in time.");
        }
    }


}
