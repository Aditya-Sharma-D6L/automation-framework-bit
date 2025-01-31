package com.example.automation.pages;

import com.example.automation.config.ApplicationProperties;
import com.example.automation.drivers.DriverManager;
import com.example.automation.utilities.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class SignupPage extends BasePage {

    @Autowired
    private DriverManager driverManager;

    @Autowired
    private ApplicationProperties appProps;

    private final HandleOtpOR2faVerification handleOtp_2fa;
    private final Captcha captcha;

    // Locators
    private static final By emailFieldXpath              = By.xpath("//input[@placeholder='Email']");
    private static final By passwordFieldXpath           = By.xpath("//input[@placeholder='Password']");
    private static final By expandReferralCodeFieldXpath = By.xpath("//label[text()='Referral code (optional)']");
    private static final By referralCodeFieldXpath       = By.xpath("//input[@name='referralCode']");
    private static final By registerButtonXpath          = By.xpath("(//button[@type='button' and text()='Register'])[2]");
    private static final By headingOfNewKycPopupXpath    = By.xpath("//h1[text()='Please provide the information']");
    private static final By registerButtonInHeader       = By.xpath("//button[@type='button' and text()='Register']");
    private static final By errorMessage                 = By.xpath("//p[text()='Registration failed. Please try login.']");
    private static final By is_signup_successful         = By.xpath("//div/p[text()='Wallet'] | " +
                                                            "//h2[text()='Individual Questionnaire'] | " +
                                                            "//h1[text()='Please provide the information'] | " +
                                                            "//p[text()='Complete identity verification to unlock all features. ']");
    private static final By country_field                = By.xpath("//input[@role='combobox']");
    private static final By email_field                                          = By.xpath("//input[@placeholder='Email']");
    private static final By password_field                                       = By.xpath("//input[@placeholder='Password']");
    private static final By is_email_or_password_not_entered_error_displayed     = By.xpath("//div[text()='Please enter your Email!']");
    private static final By email_is_not_valid           = By.xpath("//*[text()='Email is not valid!']");
    private static final By validatePasswordErrorMessage = By.xpath("//*[text()='Password is not valid!'] | "+
                                                            "//*[text()='Password must be more than 10 characters'] | " +
                                                             "//*[text()='Password must be less than 16 characters']");
    private static final By unHidePassword               = By.xpath("//div[@class='chakra-input__right-element css-wydlee']//span//*[name()='svg']");
    private static final By select_corporate_user_tab    = By.xpath("//div[text()='Corporate']");

    /**
     * Constructor for SignupPage.
     * @param driver The WebDriver instance.
     */
    public SignupPage(WebDriver driver) {
        super(driver);
        this.captcha = new Captcha(driver, waitUtils);
        this.handleOtp_2fa = new HandleOtpOR2faVerification(driver, waitUtils);
    }

    /**
     * Clicks corporate user tab
     */
    public void selectCorporateUser() {
        clickElement(select_corporate_user_tab);
    }

    /**
     * Clicks the sign-up button in header
     */
    public void goToSignupPage() throws InterruptedException {
        log.info("Clicking register button in header");
        Thread.sleep(3000);
        clickElement(registerButtonInHeader);
    }

    /**
     * Enters the country name in the "Country" field.
     *
     * @param country The country name to enter.
     */
    public void selectCountry(String country) throws Exception {
        log.info("Entering country: {}", country);

        try {
            Retry.retryOperation(() -> {
                if (!country.isEmpty()) {
                    waitUtils.waitForClickabilityShort(country_field).sendKeys(country);
                    WebElement selectCountry = waitUtils.waitForVisibilityShort(By.xpath("//div[text()='" + country + "']"));
                    selectCountry.click();
                }
                return null; // Return null since click doesn't return anything
            }, 2, 1000, "Entering country name"); // Retry 3 times with 1-second delay
        } catch (Exception e) {
            System.err.println("Failed to click element after retries: " + e.getMessage());
        }
    }

    /**
     * Enters the email address in the "Email" field.
     * @param email The email to enter.
     * @return SignupPage instance for method chaining.
     */
    public SignupPage enterEmail(String email) {
        log.info("Entering email: {}", email);
        clickElement(emailFieldXpath);
        sendKeys(emailFieldXpath, email); // Reusing BasePage's sendKeys method
        return this;
    }

    /**
     * Enters the password in the "Password" field.
     * @param password The password to enter.
     * @return SignupPage instance for method chaining.
     */
    public SignupPage enterPassword(String password) {
        log.info("Entering password: {}", password);
        clickElement(passwordFieldXpath);
        sendKeys(passwordFieldXpath, password); // Reusing BasePage's sendKeys method
        return this;
    }

    /**
     * Expands the referral code field.
     * @return SignupPage instance for method chaining.
     */
    public SignupPage expandReferralCodeField() {
        log.info("Expanding the referral code field.");
        clickElement(expandReferralCodeFieldXpath); // Reusing BasePage's clickElement method
        return this;
    }

    /**
     * Enters the referral code in the "Referral Code" field.
     * @param referralCode The referral code to enter.
     * @return SignupPage instance for method chaining.
     */
    public SignupPage enterReferralCode(String referralCode) {
        log.info("Entering referral code: {}", referralCode);
        sendKeys(referralCodeFieldXpath, referralCode);
        return this;
    }

    /**
     * Clicks the "Sign Up" button with retries.
     *
     * @throws Exception If the operation fails after the retries.
     */
    public void clickSignUpButton(String email, String password) throws Exception {
        WebElement registerButton = waitUtils.waitForPresenceLong(registerButtonXpath);
        registerButton.click();

        // re-try mechanism to enter email and password if they were not entered initially
        try {
            WebElement isEmailOrPasswordNotEnteredInInput = waitUtils.waitForVisibilityShort(is_email_or_password_not_entered_error_displayed);

            boolean flag = isEmailOrPasswordNotEnteredInInput.isDisplayed();
            while (flag) {
                waitUtils.waitForVisibilityShort(email_field).sendKeys(email);
                waitUtils.waitForVisibilityShort(password_field).sendKeys(password);
                registerButton.click();

                flag = isEmailOrPasswordNotEnteredInInput.isDisplayed();
            }
        } catch (Exception e) {
            //
        }
    }

    /**
     * Clicks the register button without retries
     */
    public void clickRegisterButton() {
        WebElement registerButton = waitUtils.waitForPresenceLong(registerButtonXpath);
        registerButton.click();
    }

    /**
     * Verifies if error is displayed when trying to register with existing email
     * @return Text message of the error message
     */
    public String getExistingEmailErrorMessage() {
        log.info("Checking if system shows error when trying to register using existing email.");
        WebElement errorMessageText = waitUtils.waitForVisibilityShort(errorMessage);
        return errorMessageText.getText();
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
     * Verifies if the KYC popup appears after registration.
     * @return True if the KYC popup is displayed, false otherwise.
     */
    public boolean isKycPopupDisplayed() {
        log.info("Checking if the KYC popup is displayed.");
        return elementUtils.isElementDisplayed(headingOfNewKycPopupXpath); // Reusing BasePage's isElementDisplayed method
    }

    /**
     * Enters the otp or 2FA
     */
    public void enterOtpOr2Fa() throws InterruptedException {
        handleOtp_2fa.handleOtp();
    }

    /**
     * Verifies of the signup process if successfully completed or not
     * @return Boolean if the header is displayed with wallet or if general survey is displayed
     */
    public boolean isSignUpSuccessful() {
        return waitUtils.waitForVisibilityLong(is_signup_successful).isDisplayed();
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
     * Utility method to test password validation.
     *
     * @param password The password to input.
     * @param expectedErrorMessage The expected error message.
     */
    public boolean validatePassword(String email, String password, String expectedErrorMessage) throws Exception {
        System.out.println();
        log.info("Case: {}", expectedErrorMessage);

        enterPassword(password);
        clickSignUpButton(email, password);

        return waitUtils.waitForVisibilityShort(validatePasswordErrorMessage).isDisplayed();
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
     * Checks whether the OTP or 2FA is displayed or not
     * @return Boolean if either of the pages are found
     */
    public boolean isOtpOr2FaPageDisplayed() {
        return handleOtp_2fa.isOtpOr2FaPageDisplayed();
    }

    public void signUp(String dynamicEmail, String password) throws Exception {

        String baseUrl = appProps.getBaseUrl();
        driver.get(baseUrl);
        log.info("Navigating to Signup Page: {}", baseUrl + "/register");

        // Accept platform TnC if displayed
        acceptTnc();

        goToSignupPage();

        // Step 2: Fill out the signup form and submit
        log.info("Using dynamic email for signup: {}", dynamicEmail);

        enterEmail(dynamicEmail);
        enterPassword(password);
        Thread.sleep(1000);
        clickSignUpButton(dynamicEmail, password);

        // Step 3: Post Signup Validation
        String expectedUrl = baseUrl + "/register";
        log.info("Validating redirection to: {}", expectedUrl);

        // Step 4: Handle Terms and Conditions Modal
        acceptTnc();

        // Step 5: Solve captcha
        solveCaptcha();

        // Step 6: Verify OTP/2FA
        enterOtpOr2Fa();

        // Step 7: Verify if signup is successful
        if(isSignUpSuccessful()) {
            log.info("Registration successful");
        }

        log.info("Signup Test With Valid Credentials Completed Successfully.");
    }
}
