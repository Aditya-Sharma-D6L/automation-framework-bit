package com.example.automation.pages;

import com.example.automation.config.ApplicationProperties;
import com.example.automation.drivers.DriverManager;
import com.example.automation.utilities.Retry;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import com.example.automation.utilities.WaitUtils;
import com.example.automation.utilities.ElementUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class BasePage {

    protected WebDriver driver;
    protected WaitUtils waitUtils;
    protected ElementUtils elementUtils;
    protected int longWait = 10;
    protected int shortWait = 5;

    /**
     * Constructor for BasePage.
     * @param driver The WebDriver instance.
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver, longWait, shortWait);
        this.elementUtils = new ElementUtils(driver, longWait);

        log.info("Setting long wait timeout to {} seconds.", longWait);
        log.info("Setting short wait timeout to {} seconds.", shortWait);
    }

    // Core methods for interacting with the page, e.g., navigation, clicks, etc.
    /**
     * Retrieves the current page title.
     * @return Page title.
     */
    public String getTitle() {
        try {
            String title = driver.getTitle();
            log.info("Current page title: {}", title);
            return title;
        } catch (Exception e) {
            log.error("Failed to get page title", e);
            throw new RuntimeException("Failed to get page title", e);
        }
    }

    /**
     * Retrieves the current URL.
     * @return Current URL.
     */
    public String getCurrentUrl() {
        try {
            String url = driver.getCurrentUrl();
            log.info("Current URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Failed to get current URL", e);
            throw new RuntimeException("Failed to get current URL", e);
        }
    }

    /**
     * Clicks an element after waiting for it to be clickable.
     * @param locator The By locator of the element.
     */
    public void clickElement(By locator) {
        try {
            WebElement element = waitUtils.waitForClickabilityLong(locator);
            log.info("Clicking element located by: {}", locator);
            Retry.retryOperation(() -> {
                element.click();
                return null;
            }, 2, 500, "");
        } catch (Exception e) {
            log.error("Failed to click element located by: {}", locator, e);
            throw new RuntimeException("Failed to click element: " + locator, e);
        }
    }

    /**
     * Sends keys to an element after waiting for it to be visible.
     * @param locator The By locator of the element.
     * @param text The text to enter.
     */
    public void sendKeys(By locator, String text) {
        try {
            WebElement element = waitUtils.waitForVisibilityLong(locator);
            log.info("Sending keys to element located by: {}. Email: {}", locator, text);
//            element.clear();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value = '';", element); // to clear the password field

            element.sendKeys(text);
        } catch (Exception e) {
            log.error("Failed to send keys to element located by: {}", locator, e);
            throw new RuntimeException("Failed to send keys to element: " + locator, e);
        }
    }
}
