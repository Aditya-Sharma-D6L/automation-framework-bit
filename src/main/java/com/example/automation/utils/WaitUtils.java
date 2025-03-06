package com.example.automation.utils;

import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtils {

    private final WebDriver driver;

    /**
     * -- GETTER --
     *  Returns the WebDriverWait instance for long waits.
     *
     */
    @Getter
    private final WebDriverWait longWait;

    /**
     * -- GETTER --
     *  Returns the WebDriverWait instance for short waits.
     *
     */
    @Getter
    private final WebDriverWait shortWait;

    /**
     * Constructor for WaitUtils.
     *
     * @param driver            The WebDriver instance.
     * @param longWaitDuration  Timeout in seconds for long waits.
     * @param shortWaitDuration Timeout in seconds for short waits.
     */
    public WaitUtils(WebDriver driver, int longWaitDuration, int shortWaitDuration) {
        this.driver = driver;
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(longWaitDuration));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(shortWaitDuration));
    }

    /**
     * Waits for an element to be visible on the page using a long wait.
     *
     * @param locator The locator of the element.
     * @return The visible WebElement.
     */
    public WebElement waitForVisibilityLong(By locator) {
        return waitForVisibility(locator, longWait);
    }

    /**
     * Waits for an element to be visible on the page using a short wait.
     *
     * @param locator The locator of the element.
     * @return The visible WebElement.
     */
    public WebElement waitForVisibilityShort(By locator) {
        return waitForVisibility(locator, shortWait);
    }

    /**
     * Generic method to wait for visibility using a specified WebDriverWait.
     *
     * @param locator The locator of the element.
     * @param wait    The WebDriverWait instance to use.
     * @return The visible WebElement.
     */
    private WebElement waitForVisibility(By locator, WebDriverWait wait) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element not visible: " + locator, e);
        }
    }

    /**
     * Waits for an element to be invisible on the page using a short wait.
     *
     * @param locator The locator of the element.
     * @return True if the element is invisible, false otherwise.
     */
    public boolean waitForInvisibilityShort(By locator) {
        return waitForInvisibility(locator, shortWait);
    }

    /**
     * Waits for an element to be invisible on the page using a long wait.
     *
     * @param locator The locator of the element.
     * @return True if the element is invisible, false otherwise.
     */
    public boolean waitForInvisibilityLong(By locator) {
        return waitForInvisibility(locator, longWait);
    }

    /**
     * Generic method to wait for an element to become invisible.
     *
     * @param locator The locator of the element.
     * @param wait    The WebDriverWait instance to use.
     * @return True if the element is invisible, false otherwise.
     */
    private boolean waitForInvisibility(By locator, WebDriverWait wait) {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element did not become invisible: " + locator, e);
        }
    }

    /**
     * Waits for an element to be clickable using a short wait.
     *
     * @param locator The locator of the element.
     * @return The clickable WebElement.
     */
    public WebElement waitForClickabilityShort(By locator) {
        return waitForClickability(locator, shortWait);
    }

    /**
     * Waits for an element to be clickable using a long wait.
     *
     * @param locator The locator of the element.
     * @return The clickable WebElement.
     */
    public WebElement waitForClickabilityLong(By locator) {
        return waitForClickability(locator, longWait);
    }

    /**
     * Generic method to wait for an element to be clickable.
     *
     * @param locator The locator of the element.
     * @param wait    The WebDriverWait instance to use.
     * @return The clickable WebElement.
     */
    private WebElement waitForClickability(By locator, WebDriverWait wait) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element not clickable: " + locator, e);
        }
    }

    /**
     * Waits for an element to be present in the DOM using a long wait.
     *
     * @param locator The locator of the element.
     * @return The WebElement.
     */
    public WebElement waitForPresenceLong(By locator) {
        return waitForPresence(locator, longWait);
    }

    /**
     * Waits for an element to be present in the DOM using a short wait.
     *
     * @param locator The locator of the element.
     * @return The WebElement.
     */
    public WebElement waitForPresenceShort(By locator) {
        return waitForPresence(locator, shortWait);
    }

    /**
     * Generic method to wait for an element to be present in the DOM.
     *
     * @param locator The locator of the element.
     * @param wait    The WebDriverWait instance to use.
     * @return The WebElement.
     */
    private WebElement waitForPresence(By locator, WebDriverWait wait) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element not present: " + locator, e);
        }
    }
}
