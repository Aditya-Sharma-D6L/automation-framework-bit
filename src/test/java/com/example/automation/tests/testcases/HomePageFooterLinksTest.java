package com.example.automation.tests.testcases;

import com.example.automation.config.ApplicationProperties;
import com.example.automation.drivers.DriverManager;
import com.example.automation.pages.HomePage;
import com.example.automation.utilities.ScreenshotUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@Slf4j
@SpringBootTest
public class HomePageFooterLinksTest extends BaseTest {

    @Autowired
    private DriverManager driverManager;

    @Autowired
    private ApplicationProperties appProps;

    private HomePage homePage;

    // ANSI escape codes for colors
    private static final String RESET = "\u001B[0m";  // Resets the text color
    private static final String GREEN = "\u001B[32m"; // Green color for passed
    private static final String RED = "\u001B[31m";   // Red color for failed
    private static final String YELLOW = "\u001B[33m"; // Yellow color for skipped or informational

    @BeforeMethod
    public void setup(ITestResult result) throws Exception {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        initializeDriver(testName, description);

        homePage = new HomePage(driver);
    }

    /**
     * Verifies all footer links on the Home Page.
     *
     */
    @Test(description = "Check the footer links for validity and responsiveness.")
    public void verifyHomePageFooterLinks() {
        homePage = new HomePage(driver);
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
            log.error(RED + "{}" + RESET, errorMessage);
            test.fail(errorMessage);
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }
}
