package com.example.automation.tests.testcases;

import com.aventstack.extentreports.ExtentTest;
import com.example.automation.config.ApplicationProperties;
import com.example.automation.drivers.DriverManager;
import com.example.automation.pages.SignupPage;
import com.example.automation.tests.testdata.PasswordValidationData;
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
public class SignUpTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DriverManager driverManager;

    @Autowired
    private ApplicationProperties appProps;

    @Autowired
    private GenerateEmail generateEmail;

    private SignupPage signupPage;
    private WebDriver driver;
    private String baseUrl;
    private ExtentTest test;

    private void initializeDriver(String testName, String description) throws Exception {
        driverManager.initializeDriver();
        driver = driverManager.getDriver();
        signupPage = new SignupPage(driver);

        // Create a test instance in Extent Reports
        test = ExtentReportManager.createTest(testName, description);

        baseUrl = appProps.getBaseUrl();
        driver.get(baseUrl);
        test.info("Navigating to Signup Page: " + baseUrl + "/register");
        log.info("Navigating to Signup Page: {}", baseUrl + "/register");

        // Accept platform TnC if displayed
        signupPage.acceptTnc();
        test.info("Accepted TnC");

        // Toggle dark theme
        ToggleTheme.enableDarkTheme(driver, new WaitUtils(driver, 5, 10));

        signupPage.goToSignupPage();
        test.info("Navigated to the signup page");
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

    @Test(priority = 1, description = "Verify that the signup form doesnt submit with empty email and password fields")
    public void testWithEmptyEmailAndPassword() throws Exception {
        initializeDriver("Verify signup with empty form", "Sign-Up form should not submit with empty form");
        test.info("Test for signup with empty form");

        try {
            String email = "";
            String password = "";
            signupPage.enterEmail(email);
            signupPage.enterPassword(password);
            log.info("Clicking register button 10 times");
            for (int i = 1; i <= 10; i++) {
                System.out.println("Register button is clicked : " + i + " times");
                Thread.sleep(500);
                signupPage.clickRegisterButton();
            }

            Assert.assertFalse(signupPage.isOtpOr2FaPageDisplayed(), "OTP/2FA page is displayed");
        } catch (Exception e) {
            test.info("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test(priority = 2, groups = {"regression", "sanity"}, description = "Registration with invalid email 'abcd.com'")
    public void testSignUpWithInvalidEmail() throws Exception {
        initializeDriver("Sign-Up with Invalid Email", "Test for invalid email format validation");

        try {
            signupPage.enterEmail("abcd.com").enterPassword("Pass@12345").clickSignUpButton("abcd.com", "Pass@12345");

            Assert.assertEquals(signupPage.getInvalidEmailErrorMessage(), "Email is not valid!");
            test.pass("Invalid email validation passed");
        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 3, groups = {"regression"}, description = "Verify sign-up functionality with existing email")
    public void testSignUpWithExistingEmail() throws Exception {
        initializeDriver("Sign-Up with Existing Email", "Test for existing email validation");

        try {
            String existingEmail = EnvironmentUtils.getEmailForEnvironment();
            String password = "Pass@12345";

            signupPage.enterEmail(existingEmail).enterPassword(password);

            for (int i = 1; i <= 5; i++) {
                signupPage.clickRegisterButton();
            }
            Assert.assertEquals(signupPage.getExistingEmailErrorMessage(), "Registration failed. Please try login.");

            test.pass("Sign-up with existing email verified successfully");
        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 4, groups = {"regression", "smoke"}, description = "Test for successful sign-up with valid(unique) email and password")
    public void testSignUpWithValidEmailAndPassword() throws Exception {
        initializeDriver("Sign-Up with Valid Email and Password", "Test for successful sign-up");
        test.info("Test for successful sign-up with valid(unique) email and password");

        try {
            String dynamicEmail = generateEmail.generateEmail();
            String password = "Pass@12345";
            test.info("Generated email: " + dynamicEmail);

            signupPage.signUp(dynamicEmail, password);
            signupPage.enterOtpOr2Fa();

            Assert.assertTrue(signupPage.isSignUpSuccessful(), "Registration not successful");
            test.pass("Sign-up with valid email and password completed successfully");
        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 5, groups = {"regression"}, description = "Verify registration other than India as a country")
    public void testSignUpWithDifferentCountry() throws Exception {
        initializeDriver("Signup with different country other than India", "Test for different country during signup");

        // Fill out the signup form and submit
        String country = "Poland";
        String email = generateEmail.generateEmail();
        String password = "Pass@12345";
        log.info("Using country for signup: {}", country);

        Thread.sleep(1000);
        signupPage.selectCountry(country);
        signupPage.signUp(email, password);

        // Verify OTP/2FA
        signupPage.enterOtpOr2Fa();

        // Verify if signup is successful
        if(signupPage.isSignUpSuccessful()) {
            log.info("Registration successful");
        }

        log.info("Signup completed With {} as country.", country);

    }

    @Test(priority = 6, groups = {"regression", "sanity"}, description = "Verify registration with referral code")
    public void testSignUpWithReferralCode() throws Exception {
        initializeDriver("Signup using referral code", "Test signup using a referral code");

        // Fill out the signup form and submit
        String referralCode = "r5fya3_R";
        String email = generateEmail.generateEmail();
        String password = "Pass@12345";
        log.info("Using referral code for signup: {}", referralCode);

        Thread.sleep(1000);
        signupPage.enterEmail(email)
                .enterPassword(password)
                .expandReferralCodeField()
                .enterReferralCode(referralCode)
                .clickSignUpButton(email, password);
        signupPage.acceptTnc();
        signupPage.solveCaptcha();
        signupPage.enterOtpOr2Fa();

        // Verify if signup is successful
        if(signupPage.isSignUpSuccessful()) {
            log.info("Registration successful");
        }

        log.info("Signup completed With {} as referral code.", referralCode);
    }

    @Test(priority = 7, groups = {"regression", "sanity"},
            description = "Verify registration with various invalid password formats",
            dataProvider = "passwordValidationDataProvider",
            dataProviderClass = PasswordValidationData.class)
    public void testDynamicPasswordValidationRules(String password, String expectedMessage) throws Exception {
        initializeDriver("Password Test: " + expectedMessage, "Test various password validation rules");

        try {
            String email = generateEmail.generateEmail();
            signupPage.enterEmail(email).showPassword();

            boolean validationFlag = signupPage.validatePassword(email, password, expectedMessage);
            Assert.assertTrue(validationFlag, "Password validation failed for: " + password);

            test.pass("Password validation passed for: " + password);
        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 8, description = "Verify corporate user registration")
    public void testSignUpWithCorporateUser() throws Exception {
        initializeDriver("Test signup with corporate user", "Verify corporate user registration");

        try {
            String email = generateEmail.generateEmail();
            String password = "Pass@12345";

            signupPage.corpSignUp(email, password);

            signupPage.enterOtpOr2Fa();
            signupPage.isSignUpSuccessful();

        } catch (Exception e) {
            test.fail("Test failed due to an exception: " + e.getMessage());
            ScreenshotUtil.captureScreenshot(driver, test, getMethodName());
            throw e;
        }
    }

    @Test(priority = 9, description = "Verify corporate user registration")
    public void testSignUpWithCorporateUserWithDifferentCountry() throws Exception {
        initializeDriver("Test signup with corporate user", "Verify corporate user registration");

        try {
            String email = generateEmail.generateEmail();
            String password = "Pass@12345";
            String country = "Germany";

            signupPage.selectCountry(country);
            signupPage.corpSignUp(email, password);
            signupPage.enterOtpOr2Fa();

            Assert.assertTrue(signupPage.isSignUpSuccessful(), "Registration failed, something went wrong");
            log.info("Signup Test With Valid Credentials Completed Successfully.");

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
