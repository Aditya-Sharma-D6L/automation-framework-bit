package com.example.automation.drivers;

import com.example.automation.config.ApplicationProperties;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DriverManager {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, String> BRAVE_PATHS = new ConcurrentHashMap<>();

    static {
        BRAVE_PATHS.put("win", "C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe");
        BRAVE_PATHS.put("mac", "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser");
        BRAVE_PATHS.put("linux", "/usr/bin/brave-browser");
    }

    @Autowired
    private ApplicationProperties appProps;

    /**
     * Initializes the WebDriver for the current thread.
     */
    public void initializeDriver() {
        if (driverThreadLocal.get() != null) {
            log.warn("WebDriver is already initialized for this thread. Reinitializing...");
            quitDriver(); // Ensure the existing driver is cleaned up
        }

        String browser = appProps.getBrowser().toLowerCase();
        log.info("Initializing WebDriver for browser: {}", browser);
        WebDriver driver;

        switch (browser) {
            case "firefox" -> driver = initializeFirefoxDriver();
            case "edge" -> driver = initializeEdgeDriver();
            case "safari" -> driver = initializeSafariDriver();
            case "brave" -> driver = initializeBraveDriver();
            default -> driver = initializeChromeDriver();
        }

        driver.manage().window().maximize();
        driverThreadLocal.set(driver);
    }

    /**
     * Returns the WebDriver instance for the current thread.
     */
    public WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            throw new IllegalStateException("Driver not initialized. Call initializeDriver() first.");
        }
        return driverThreadLocal.get();
    }

    /**
     * Quits the WebDriver and removes it from ThreadLocal.
     */
    public void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            log.info("Quitting WebDriver for the current thread.");
            try {
                driver.quit();
            } catch (Exception e) {
                log.warn("Error while quitting WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
                waitThreadLocal.remove();
                log.info("WebDriver and WebDriverWait instances removed from ThreadLocal.");
            }
        } else {
            log.warn("WebDriver is already null. Nothing to quit.");
        }
    }

    /**
     * Returns the WebDriverWait instance for the current thread.
     */
    public WebDriverWait getWait() {
        if (waitThreadLocal.get() == null) {
            throw new IllegalStateException("WebDriverWait not initialized. Call initializeDriver() first.");
        }
        return waitThreadLocal.get();
    }

    /**
     * Initializes ChromeDriver.
     */
    private WebDriver initializeChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        return new ChromeDriver(options);
    }

    /**
     * Initializes FirefoxDriver.
     */
    private WebDriver initializeFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--disable-notifications");
        return new FirefoxDriver(options);
    }

    /**
     * Initializes EdgeDriver.
     */
    private WebDriver initializeEdgeDriver() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-notifications");
        return new EdgeDriver(options);
    }

    /**
     * Initializes SafariDriver.
     */
    private WebDriver initializeSafariDriver() {
        WebDriverManager.safaridriver().setup();
        return new SafariDriver();
    }

    /**
     * Initializes BraveDriver.
     */
    private WebDriver initializeBraveDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions braveOptions = new ChromeOptions();
        braveOptions.addArguments("--disable-notifications");
        braveOptions.setBinary(detectBravePath());
        return new ChromeDriver(braveOptions);
    }

    /**
     * Detects the Brave browser binary path based on the OS.
     */
    private String detectBravePath() {
        String os = System.getProperty("os.name").toLowerCase();
        log.info("Detecting Brave browser path for OS: {}", os);

        return BRAVE_PATHS.entrySet().stream()
                .filter(entry -> os.contains(entry.getKey()))
                .findFirst()
                .map(entry -> {
                    log.info("Detected Brave binary path: {}", entry.getValue());
                    return entry.getValue();
                })
                .orElseThrow(() -> new RuntimeException("Brave browser not supported on OS: " + os));
    }
}
