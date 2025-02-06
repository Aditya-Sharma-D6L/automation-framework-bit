package com.example.automation.tests.testcases;

import com.aventstack.extentreports.ExtentTest;
import com.example.automation.config.ApplicationProperties;
import com.example.automation.drivers.DriverManager;
import com.example.automation.pages.HomePage;
import com.example.automation.utilities.ExtentReportManager;
import com.example.automation.utilities.ScreenshotUtil;
import com.example.automation.utilities.ToggleTheme;
import com.example.automation.utilities.WaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.List;

@Slf4j
@SpringBootTest
public class HomePageFooterLinksTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DriverManager driverManager;

    @Autowired
    private ApplicationProperties appProps;

    private HomePage homePage;
    private WebDriver driver;
    private ExtentTest test;

    // ANSI escape codes for colors
    private static final String RESET = "\u001B[0m";  // Resets the text color
    private static final String GREEN = "\u001B[32m"; // Green color for passed
    private static final String RED = "\u001B[31m";   // Red color for failed
    private static final String YELLOW = "\u001B[33m"; // Yellow color for skipped or informational

    /**
     * Initializes the WebDriver and HomePage objects, navigates to the base URL, and applies initial setups.
     *
     * @param testName     The name of the test for reporting.
     * @param description  A brief description of the test.
     * @throws Exception Throws an exception if initialization fails.
     */
    private void initializeDriver(String testName, String description) throws Exception {
        driverManager.initializeDriver();
        driver = driverManager.getDriver();

        test = ExtentReportManager.createTest(testName, description);

        String baseUrl = appProps.getBaseUrl();
        driver.get(baseUrl);
        log.info("Navigating to Home Page: {}", baseUrl);
        test.info("Navigating to Home Page: " + baseUrl);

        homePage = new HomePage(driver);
        homePage.acceptTnc();
        test.info("Accepted Terms and Conditions popup.");

        // Toggle dark theme
        ToggleTheme.enableDarkTheme(driver, new WaitUtils(driver, 5, 10));
        test.info("Enabled dark theme.");
    }

    /**
     * Verifies all footer links on the Home Page.
     *
     * @throws Exception Throws an exception if validation fails.
     */
    @Test(description = "Verify footer links on the home page")
    public void verifyHomePageFooterLinks() throws Exception {
        initializeDriver("Verify Home Page Footer Links", "Check the footer links for validity and responsiveness.");
        test.info("Starting footer links verification.");

        try {
            List<WebElement> links = homePage.getFooterLinks();
            log.info("Total footer links found: {}", links.size());
            test.info("Total footer links found: " + links.size());

            int[] counts = {0, 0, 0}; // {passed, failed, skipped}
            for (WebElement link : links) {
                String result = homePage.validateLink(link, counts);

                // Log and update test report based on the result
                if (result.startsWith("PASSED")) {
                    log.info(GREEN + "{}" + RESET, result);
                    test.pass(result);
                } else if (result.startsWith("FAILED")) {
                    log.warn(RED + "{}" + RESET, result);
                    test.fail(result);
                } else if (result.startsWith("SKIPPED")) {
                    log.info(YELLOW + "{}" + RESET, result);
                    test.warning(result);
                }
            }

            // Summary log and assertions
            log.info("\n--- Summary ---");
            log.info(GREEN + "PASSED: {}" + RESET, counts[0]);
            log.info(RED + "FAILED: {}" + RESET, counts[1]);
            log.info(YELLOW + "SKIPPED: {}" + RESET, counts[2]);

            System.out.println("\n--- Broken Links ---");
            for (String brokenLink: homePage.getBrokenLinks()) {
                System.out.println(RED + brokenLink + RESET);
            }
        } catch (Exception e) {
            String errorMessage = "Test failed due to an exception: " + e.getMessage();
            log.error(RED + errorMessage + RESET);
            test.fail(errorMessage);
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    /**
     * Retrieves the name of the currently executing method.
     *
     * @return The method name as a string.
     */
    private String getMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    /**
     * Closes the WebDriver instance and flushes the ExtentReport.
     */
    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            log.info("Driver quit successfully.");
            test.info("Driver quit successfully.");
        }
        ExtentReportManager.flushReports();
    }
}
