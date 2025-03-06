package com.example.automation.tests.testcases;

import com.example.automation.pages.LoginPage;
import com.example.automation.tests.testdata.UserCredentials;
import com.example.automation.tests.utilities.EnvironmentUtils;
import com.example.automation.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
@SpringBootTest
public class LoginTest extends BaseTest {

    @Autowired
    private GenerateEmail generateEmail;

    private LoginPage loginPage;

    @BeforeMethod
    public void setup(ITestResult result) throws Exception {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        initializeDriver(testName, description);

        loginPage = new LoginPage(driver);
        loginPage.goToLoginPage();
    }

    @Test(priority = 1, description = "Login form should not go to password page")
    public void testLoginWithInvalidEmail() throws Exception {
        test.info("Test for login with empty form");

        try {
            String email = "abcd.com";

            loginPage.enterEmail(email);
            loginPage.clickLoginButton();

            Assert.assertEquals(loginPage.getInvalidEmailErrorMessage(), "Email is not valid!");
            test.pass("Invalid email validation passed");

        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }

    @Test(priority = 2, description = "Login form should not go to password page")
    public void testLoginWithEmptyEmail() throws Exception {
        test.info("Test for login with empty form");

        try {
            loginPage.enterEmail("");
            for (int i = 1; i <= 10; i++) {
                System.out.println("Login button is clicked : " + i + " times");
                waitForSeconds(1);
                loginPage.clickLoginButton();
            }

            Assert.assertEquals(loginPage.getEmptyEmailErrorMessage(), "Please enter your Email!");
            test.pass("Empty email validation passed");

        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }

    @Test(priority = 3, description = "Verify login with non-existing user")
    public void testLoginWithNonExistingEmail() throws Exception {
        test.info("Test for login with non-existing email");

        try {
            String email = generateEmail.generateEmail();

            loginPage.enterEmail(email);
            for (int i = 1; i <= 10; i++) {
                System.out.println("Login button is clicked : " + i + " times");
                waitForSeconds(1);
                loginPage.clickLoginButton();
            }

            Assert.assertEquals(loginPage.getInvalidEmailErrorMessage(), "Invalid Email");
            test.pass("Empty email validation passed");

        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }

    @Test(priority = 4, description = "Login form should not submit")
    public void testLoginWithInvalidPassword() throws Exception {
        test.info("Test for login with invalid password");

        try {
            String email = EnvironmentUtils.getEmailForEnvironment();
            String password = "Pass@123456789";

            loginPage.login(email, password);

            Assert.assertTrue(loginPage.getInvalidPasswordErrorMessage().contains("Please enter correct credentials, you have"), "Please enter correct credentials, you have");
            test.pass("Empty email validation passed");

        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }

    @Test(priority = 5,
            description = "Verify login with correct email and password",
            dataProviderClass = UserCredentials.class,
            dataProvider = "userCredentials")
    public void testLogin(String env, String email, String password) throws Exception {
        test.info("Test for login with valid email and password");

        try {
            loginPage.login(email, password);

//            if (env.equals("prod")) {
//                generateTOTPForUser(email);
//            } else {
//                loginPage.enterOtpOr2Fa();
//            }

            loginPage.enterOtpOr2Fa();

            Assert.assertTrue(loginPage.isLoginSuccessful(), "Login Failed. Something went wrong");
            test.pass("Login successful");

        } catch (Exception e) {
            captureFailureDetails(e);
        }
    }
}
