package com.example.automation.tests.testcases;

import com.aventstack.extentreports.ExtentTest;
import com.example.automation.config.ApplicationProperties;
import com.example.automation.config.Config;
import com.example.automation.drivers.DriverManager;
import com.example.automation.pages.LoginPage;
import com.example.automation.tests.testdata.UserCredentials;
import com.example.automation.tests.utilities.EnvironmentUtils;
import com.example.automation.utilities.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@Slf4j
@SpringBootTest
public class LoginTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DriverManager driverManager;

    @Autowired
    private ApplicationProperties appProps;

    @Autowired
    private GenerateEmail generateEmail;

    private LoginPage loginPage;
    private WebDriver driver;
    private String env;
    private String baseUrl;
    private ExtentTest test;

    private void initializeDriver(String testName, String description) throws Exception {
        driverManager.initializeDriver();
        driver = driverManager.getDriver();
        loginPage = new LoginPage(driver, 10);

        // Create a test instance in Extent Reports
        test = ExtentReportManager.createTest(testName, description);

        baseUrl = appProps.getBaseUrl();
        driver.get(baseUrl);
        test.info("Navigating to Login Page: " + baseUrl + "/login");
        log.info("Navigating to Login Page: {}", baseUrl + "/login");

        // Accept platform TnC if displayed
        loginPage.acceptTnc();
        test.info("Accepted TnC");

        // Toggle dark theme
        ToggleTheme.enableDarkTheme(driver, new WaitUtils(driver, 5, 10));

        loginPage.goToLoginPage();
        test.info("Navigated to the login page");
    }

    @Test(priority = 0)
    public void verifyBeans() {
        test = ExtentReportManager.createTest("Verify Beans", "Verify application beans are loaded for " + this.getClass().getSimpleName());
        try {
            assertNotNull(driverManager, "DriverManager bean is null");
            assertNotNull(appProps, "ApplicationProperties bean is null");
            test.pass("Beans are loaded successfully");
        } catch (AssertionError e) {
            test.fail("Beans verification failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 1, description = "Verify login with invalid email")
    public void testLoginWithInvalidEmail() throws Exception {
        initializeDriver("Verify login with invalid email", "Login form should not go to password page");
        test.info("Test for login with empty form");

        try {
            String email = "abcd.com";

            loginPage.enterEmail(email);
            loginPage.clickLoginButton();

            Assert.assertEquals(loginPage.getInvalidEmailErrorMessage(), "Email is not valid!");
            test.pass("Invalid email validation passed");

        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 2, description = "Verify login with empty email")
    public void testLoginWithEmptyEmail() throws Exception {
        initializeDriver("Verify login with empty email", "Login form should not go to password page");
        test.info("Test for login with empty form");

        try {
            String email = "";

            loginPage.enterEmail(email);
            for (int i = 1; i <= 10; i++) {
                System.out.println("Login button is clicked : " + i + " times");
                Thread.sleep(500);
                loginPage.clickLoginButton();
            }

            Assert.assertEquals(loginPage.getEmptyEmailErrorMessage(), "Please enter your Email!");
            test.pass("Empty email validation passed");

        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 3, description = "Verify login with non-existing user")
    public void testLoginWithNonExistingEmail() throws Exception {
        initializeDriver("Verify login with non-existing user", "Login form should not go to password page");

        test.info("Test for login with non-existing email");

        try {
            String email = generateEmail.generateEmail();

            loginPage.enterEmail(email);
            for (int i = 1; i <= 10; i++) {
                System.out.println("Login button is clicked : " + i + " times");
                Thread.sleep(1000);
                loginPage.clickLoginButton();
            }

            Assert.assertEquals(loginPage.getInvalidEmailErrorMessage(), "Invalid Email");
            test.pass("Empty email validation passed");

        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 4, description = "Verify login with valid email but invalid password")
    public void testLoginWithInvalidPassword() throws Exception {
        initializeDriver("Verify login with valid email but invalid password", "Login form should not submit");

        test.info("Test for login with invalid password");

        try {
            String email = EnvironmentUtils.getEmailForEnvironment();
            String password = "Pass@123456789";

            loginPage.login(email, password);

            Assert.assertTrue(loginPage.getInvalidPasswordErrorMessage().contains("Please enter correct credentials, you have"), "Please enter correct credentials, you have");
            test.pass("Empty email validation passed");

        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 5,
            description = "Verify login with correct email and password",
            dataProviderClass = UserCredentials.class,
            dataProvider = "userCredentials")
    public void testLogin(String env, String email, String password) throws Exception {
        initializeDriver("Verify login with valid email and password", "Login form should submit");

        test.info("Test for login with valid email and password");

        try {
            loginPage.login(email, password);
            loginPage.enterOtpOr2Fa();

            Assert.assertTrue(loginPage.isLoginSuccessful(), "Login Failed. Something went wrong");
            test.pass("Login successful");

        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }


    private String getMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            test.info("Driver quit successfully");
        }
        ExtentReportManager.flushReports();
    }
}
