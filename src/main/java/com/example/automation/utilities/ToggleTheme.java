package com.example.automation.utilities;

import org.openqa.selenium.*;

public class ToggleTheme {

    // Locator for the toggle theme button
    private static final By CHANGE_THEME = By.xpath("//button[@aria-label='Toggle theme mode']//*[name()='svg']");

    /**
     * Enables the dark theme using the provided WebDriver and WebDriverWait.
     *
     * @param driver The WebDriver instance.
     * @param waitUtils   The WaitUtils instance.
     */
    public static void enableDarkTheme(WebDriver driver, WaitUtils waitUtils) throws Exception {
        WebElement toggleTheme = waitUtils.waitForVisibilityShort(CHANGE_THEME);
        try {
            toggleTheme.click();
        } catch (ElementClickInterceptedException | TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            driver.navigate().refresh();
            Retry.retryOperation(() -> {
                toggleTheme.click();
                return null;
            }, 3, 1000, "Clicking theme change button");
        } catch (Exception e) {
            System.out.println("Error changing theme: " + e.getMessage());
        }
    }
}
