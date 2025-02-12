package com.example.automation.utilities;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;

@Slf4j
public class ToggleTheme {

    // Locator for the toggle theme button
    private static final By change_theme = By.xpath("//button[@aria-label='Toggle theme mode']//*[name()='svg']");

    // ThreadLocal variable to track if the theme has been set for each thread
    private static final ThreadLocal<Boolean> isThemeSet = ThreadLocal.withInitial(() -> false);

    /**
     * Enables the dark theme using the provided WebDriver and WaitUtils.
     *
     * @param driver The WebDriver instance.
     * @param waitUtils The WaitUtils instance.
     */
    public static void enableDarkTheme(WebDriver driver, WaitUtils waitUtils) throws Exception {
        // Ensure the theme is set only once per test thread
        if (!isThemeSet.get()) {
            try {
                WebElement toggleTheme = waitUtils.waitForVisibilityShort(change_theme);
                toggleTheme.click();
                isThemeSet.set(true); // Mark theme as set for this thread
                log.info("✅ Dark theme enabled successfully.");
            } catch (ElementClickInterceptedException | TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
                driver.navigate().refresh();
                Retry.retryOperation(() -> {
                    WebElement retryToggleTheme = waitUtils.waitForVisibilityShort(change_theme);
                    retryToggleTheme.click();
                    return null;
                }, 3, 1000, "Clicking theme change button");
                isThemeSet.set(true);
            } catch (Exception e) {
                System.out.println("❌ Error changing theme: " + e.getMessage());
            }
        } else {
            System.out.println("🔹 Dark theme is already set for this thread. Skipping toggle.");
        }
    }
}
