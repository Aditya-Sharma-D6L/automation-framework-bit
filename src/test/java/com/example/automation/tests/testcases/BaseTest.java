package com.example.automation.tests.testcases;

import com.aventstack.extentreports.ExtentTest;
import com.example.automation.config.ApplicationProperties;
import com.example.automation.drivers.DriverManager;
import com.example.automation.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;

@Slf4j
public class BaseTest extends AbstractTestNGSpringContextTests {

    protected WebDriver driver;
    protected WaitUtils waitUtils;
    protected ExtentTest test;

    @Autowired
    protected DriverManager driverManager;

    @Autowired
    private ApplicationProperties appProps;

    protected void initializeDriver(String testName, String description) throws Exception {
        driverManager.initializeDriver();
        driver = driverManager.getDriver();

        // Create test instance in Extent Report
        test = ExtentReportManager.createTest(testName, description);

        // Navigate to Base URL
        String baseUrl = appProps.getBaseUrl();
        driver.get(baseUrl);
        log.info("Navigating to Base URL: {}", baseUrl);
        test.info("Navigating to Base URL: " + baseUrl);

        // Accept Terms & Conditions popup
        acceptTnc();
        test.info("Accepted Terms and Conditions popup.");

        // Enable Dark Theme
        ToggleTheme.enableDarkTheme(driver, new WaitUtils(driver, 5, 10));
        test.info("Enabled dark theme.");
    }

    public void waitForSeconds(int timeInSeconds) throws InterruptedException {
        Thread.sleep(timeInSeconds * 1000L);
    }

    /**
     * Accepts TnC popup
     */
    public void acceptTnc() throws Exception {
        Retry.retryOperation(() -> {
            TermsAndConditionsModal tncModal = new TermsAndConditionsModal(driver, waitUtils);
            tncModal.acceptTermsAndConditionsPopup();
            return null;
        }, 2, 500, "Terms and Conditions Acceptance");
    }

    /**
     * Captures failure details including a screenshot and logs.
     *
     * @param e Exception that occurred
     */
    protected void captureFailureDetails(Exception e) throws Exception {
        test.fail("Test failed due to an exception: " + e.getMessage());
        ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
        log.error("Test failed", e);
        throw e;
    }

    /**
     * Retrieves the name of the currently executing method.
     *
     * @return The method name as a string.
     */
    protected String getMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    /**
     * Quits the current browser instance and flushes extent report after each class run
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            test.info("Driver quit successfully");
        }
        ExtentReportManager.flushReports();
    }
}
