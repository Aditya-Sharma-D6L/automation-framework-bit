package com.example.automation.pages;

import com.example.automation.config.ApplicationProperties;
import com.example.automation.drivers.DriverManager;
import com.example.automation.utils.Captcha;
import com.example.automation.utils.HandleOtpOR2faVerification;
import com.example.automation.utils.Retry;
import com.example.automation.utils.TermsAndConditionsModal;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LoginPage extends BasePage {

    @Autowired
    private DriverManager driverManager;

    @Autowired
    private ApplicationProperties appProps;

    private final HandleOtpOR2faVerification handleOtp_2fa;
    private final Captcha captcha;

    /**
     * Constructor for Login Page.
     * @param driver The WebDriver instance.
     */
    public LoginPage(WebDriver driver) {
        super(driver);
        this.captcha = new Captcha(driver, waitUtils);
        this.handleOtp_2fa = new HandleOtpOR2faVerification(driver, waitUtils);
    }

    // locators
    private static final By login_button_in_header       = By.xpath("//button[@type='button' and text()='Login']");
    private static final By emailFieldXpath              = By.xpath("//input[@placeholder='Email']");
    private static final By passwordFieldXpath           = By.xpath("//input[@placeholder='Password']");
    private static final By login_button                 = By.xpath("//button[contains(text(), 'Log In')]");
    private static final By email_is_not_valid           = By.xpath("//*[text()='Email is not valid!'] | " +
                                                            "//p[@class='chakra-text css-1o42aiw' and text()='Invalid Email ']");
    private static final By email_is_not_entered         = By.xpath("//*[text()='Please enter your Email!']");
    private static final By unHidePassword               = By.xpath("//div[@class='chakra-input__right-element css-1lds0jh']//div[@class='css-0']//*[name()='svg']");
    private static final By next_button_on_login_page    = By.xpath("//button[@type='submit' and text()='NEXT']");
    private static final By incorrect_password_message   = By.xpath("//p[contains(text(), 'Please enter correct credentials')]");
    private static final By is_login_successful          = By.xpath("//div/p[text()='Wallet'] | " +
                                                            "//h2[text()='Individual Questionnaire'] | " +
                                                            "//h1[text()='Please provide the information'] | " +
                                                            "//p[text()='Complete identity verification to unlock all features. ']");


    /**
     * Clicks the login button in header
     */
    public void goToLoginPage() throws InterruptedException {
        log.info("Clicking login button in header");
        Thread.sleep(3000);
        clickElement(login_button_in_header);
    }

    /**
     * Enters the email address in the "Email" field.
     * @param email The email to enter.
     * @return LoginPage instance for method chaining.
     */
    public LoginPage enterEmail(String email) {
        log.info("Entering email: {}", email);
        clickElement(emailFieldXpath);
        sendKeys(emailFieldXpath, email);
        return this;
    }

    /**
     * Enters the password in the "Password" field.
     * @param password The password to enter.
     * @return LoginPage instance for method chaining.
     */
    public LoginPage enterPassword(String password) {
        log.info("Entering password: {}", password);
        clickElement(passwordFieldXpath);
        sendKeys(passwordFieldXpath, password); // Reusing BasePage's sendKeys method
        return this;
    }

    /**
     * Clicks the "Login" button.
     */
    public void clickLoginButton() {
        WebElement loginButton = waitUtils.waitForClickabilityLong(login_button);
        loginButton.click();
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
     * Verifies if error is displayed when trying to register with existing email
     * @return Text message of the error message
     */
    public String getInvalidEmailErrorMessage() {
        log.info("Checking if system shows error when trying to register using invalid email.");
        WebElement errorMessageText = waitUtils.waitForPresenceLong(email_is_not_valid);
        return errorMessageText.getText();
    }

    /**
     * Verifies if error is displayed when trying to register with no email
     * @return Text message of the error message
     */
    public String getEmptyEmailErrorMessage() {
        log.info("Checking if system shows error when trying to register using no email.");
        WebElement errorMessageText = waitUtils.waitForPresenceLong(email_is_not_entered);
        return errorMessageText.getText();
    }

    /**
     * Verifies if error is displayed when trying to register with incorrect/invalid password
     * @return Text message of the error message
     */
    public String getInvalidPasswordErrorMessage() {
        log.info("Checking if system shows error when trying to login using incorrect/invalid password.");
        WebElement errorMessageText = waitUtils.waitForPresenceLong(incorrect_password_message);
        return errorMessageText.getText();
    }

    /**
     * Clicks the eye-icon button in password field to show password
     */
    public void showPassword() {
        clickElement(unHidePassword);
    }

    /**
     * Solves the geetest captcha until it succeeds
     */
    public void solveCaptcha() {
        log.info("Solving captcha...");
        captcha.solveGeetestCaptcha(driver);
    }

    /**
     * Clicks the next button without retries
     */
    public void submitLoginForm() {
        WebElement nextButton = waitUtils.waitForPresenceLong(next_button_on_login_page);
        nextButton.click();
    }

    /**
     * Checks if login process is successful
     * @return Boolean based on the visibility of the element in the page
     */
    public boolean isLoginSuccessful() {
        return waitUtils.waitForVisibilityLong(is_login_successful).isDisplayed();
    }

    /**
     * Handles OTP/2FA page and enters the respective code
     */
    public void enterOtpOr2Fa() {
        handleOtp_2fa.handleOtp();
    }

    /**
     * Performs the login process with valid credentials
     * @param email
     * @param password
     */
    public void login(String email, String password) {
        enterEmail(email);
        clickLoginButton();
        enterPassword(password);
        showPassword();
        submitLoginForm();
        solveCaptcha();
    }
}
