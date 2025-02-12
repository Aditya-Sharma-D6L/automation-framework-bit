package com.example.automation.tests.testcases;

import com.example.automation.pages.SignupPage;
import com.example.automation.tests.testdata.PasswordValidationData;
import com.example.automation.tests.utilities.EnvironmentUtils;
import com.example.automation.utilities.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
@SpringBootTest
public class SignUpTest extends BaseTest {

    @Autowired
    private GenerateEmail generateEmail;

    private SignupPage signupPage;

    @BeforeMethod
    public void setup(ITestResult result) throws Exception {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        initializeDriver(testName, description);

        signupPage = new SignupPage(driver);
        signupPage.goToSignupPage();
    }

    @Test(priority = 1, description = "Verify that the signup form doesnt submit with empty email and password fields")
    public void testWithEmptyEmailAndPassword() throws Exception {
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
            captureFailureDetails(e);
        }
    }

    @Test(priority = 2, groups = {"regression", "sanity"}, description = "Verify registration with invalid email")
    public void testSignUpWithInvalidEmail() throws Exception {
        try {
            signupPage.enterEmail("abcd.com").enterPassword("Pass@12345").clickSignUpButton("abcd.com", "Pass@12345");

            Assert.assertEquals(signupPage.getInvalidEmailErrorMessage(), "Email is not valid!");
            test.pass("Invalid email validation passed");
        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }

    @Test(priority = 3, groups = {"regression"}, description = "Verify sign-up functionality with existing email")
    public void testSignUpWithExistingEmail() throws Exception {
        test.info("Test for sign-up with existing email");
        try {
            String existingEmail = EnvironmentUtils.getEmailForEnvironment();
            String password = "Pass@12345";

            signupPage.enterEmail(existingEmail).enterPassword(password);

            for (int i = 1; i <= 5; i++) {
                signupPage.clickRegisterButton();
                log.info("Register button clicked {} times", i);
            }
            Assert.assertEquals(signupPage.getExistingEmailErrorMessage(), "Registration failed. Please try login.");

            test.pass("Sign-up with existing email verified successfully");
        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }

    @Test(priority = 4, groups = {"regression", "smoke"}, description = "Test for successful sign-up with valid(unique) email and password")
    public void testSignUpWithValidEmailAndPassword() throws Exception {
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
            captureFailureDetails(e);
        }
    }

    @Test(priority = 5, groups = {"regression"}, description = "Verify registration other than India as a country")
    public void testSignUpWithDifferentCountry() throws Exception {
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

        // Verify if signup is successful\
        Assert.assertTrue(signupPage.isSignUpSuccessful(), "Registration failed");
        log.info("Registration successful");
        log.info("Signup completed With {} as country.", country);
    }

    @Test(priority = 6, groups = {"regression", "sanity"}, description = "Verify registration with referral code")
    public void testSignUpWithReferralCode() throws Exception {
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
         try {
            String email = generateEmail.generateEmail();
            signupPage.enterEmail(email).showPassword();

            boolean validationFlag = signupPage.validatePassword(email, password, expectedMessage);
            Assert.assertTrue(validationFlag, "Password validation failed for: " + password);

            test.pass("Password validation passed for: " + password);
        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }

    @Test(priority = 8, description = "Verify corporate user registration")
    public void testSignUpWithCorporateUser() throws Exception {
        try {
            String email = generateEmail.generateEmail();
            String password = "Pass@12345";

            signupPage.corpSignUp(email, password);

            signupPage.enterOtpOr2Fa();
            signupPage.isSignUpSuccessful();

        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }

    @Test(priority = 9, description = "Verify corporate user registration")
    public void testSignUpWithCorporateUserWithDifferentCountry() throws Exception {
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
            captureFailureDetails(e);
        }
    }
}
