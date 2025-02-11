package com.example.automation.tests.testcases;

import com.aventstack.extentreports.ExtentTest;
import com.example.automation.config.ApplicationProperties;
import com.example.automation.drivers.DriverManager;
import com.example.automation.pages.HomePage;
import com.example.automation.utilities.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

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

    @AfterMethod
    protected void tearDown() {
        if (driver != null) {
            driverManager.quitDriver();  // ✅ Calls the thread-safe quit method from DriverManager
            log.info("Driver quit successfully.");
            test.info("Driver quit successfully.");
        }

        // ✅ Flush Extent Reports after every test method
        ExtentReportManager.flushReports();
        log.info("Extent Reports flushed.");
    }
}
