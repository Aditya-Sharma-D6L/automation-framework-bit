package com.example.automation.pages;

import com.example.automation.utils.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends BasePage {

    private final WebDriver driver;
    private final TermsAndConditionsModal handleTnC;
    private final List<String> brokenLinks = new ArrayList<>();

    // ANSI escape codes for colors
    private static final String RESET = "\u001B[0m";  // Resets the text color
    private static final String GREEN = "\u001B[32m"; // Green color for passed
    private static final String RED = "\u001B[31m";   // Red color for failed
    private static final String YELLOW = "\u001B[33m"; // Yellow color for skipped or informational

    // Locators
    private static final By footerBody = By.xpath("//div[@class='css-dh36qe']");

    public HomePage(WebDriver driver) {
        super(driver);
        this.driver = driver;
        this.handleTnC = new TermsAndConditionsModal(driver, waitUtils);
    }

    /**
    * Locates all footer links.
    *
    * @return List of WebElements representing the links in the footer.
     */
    public List<WebElement> getFooterLinks() {
        WebElement footer = waitUtils.waitForVisibilityLong(footerBody);

        // Scroll to the footer
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "const element = arguments[0];" +
                        "const headerOffset = 100;" +
                        "const elementPosition = element.getBoundingClientRect().top + window.pageYOffset - headerOffset;" +
                        "window.scrollTo({ top: elementPosition, behavior: 'smooth' });",
                footer
        );

        return footer.findElements(By.tagName("a"));
    }

    /**
     * Validates a given link by checking its HTTP status code.
     *
     * @param link The WebElement representing the link.
     * @return A String representing the result ("PASSED", "FAILED", or "SKIPPED").
     */
    public String validateLink(WebElement link, int[] counts) {
        String linkText = link.getText().trim();
        String href = link.getAttribute("href");

        // Skip links with empty linkText
        if (linkText.isEmpty()) {
            System.out.println(YELLOW + "SKIPPED: Link with no visible text found." + RESET);
            System.out.println(href);
            counts[2]++; // Increment skipped count
            return "SKIPPED";
        }

        // Skip links with empty href
        if (href == null || href.isEmpty()) {
            System.out.println(YELLOW + "SKIPPED: Link with text '" + linkText + "' has no href attribute." + RESET);
            counts[2]++; // Increment skipped count
            return "SKIPPED";
        }

        // Skip email links
        if (href.startsWith("mailto:")) {
            System.out.println(YELLOW + "SKIPPED: Link with text '" + linkText + "' is an email link (" + href + ")." + RESET);
            counts[2]++; // Increment skipped count
            return "SKIPPED";
        }

        // Detect links opening in a new tab
        String target = link.getAttribute("target");
        if ("_blank".equals(target)) {
            System.out.println(YELLOW + "INFO: Link with text '" + linkText + "' opens in a new tab." + RESET);
        }

        // Validate the link response with timeout
        try {
            int responseCode = HttpStatusCodeChecker.checkStatusCode(href);

            if (responseCode == 200) {
                System.out.println(GREEN + "PASSED: Link with text '" + linkText + "' is working fine (HTTP " + responseCode + ")." + RESET);
                counts[0]++; // Increment passed count
                return "PASSED";
            } else {
                System.out.println(RED + "FAILED: Link with text '" + linkText + "' is broken (HTTP " + responseCode + ")." + RESET);
                brokenLinks.add("Text: " + linkText + ", URL: " + href + " (HTTP " + responseCode + ")");
                counts[1]++; // Increment failed count
                return "FAILED";
            }
        } catch (java.net.SocketTimeoutException e) {
            System.out.println(YELLOW + "SKIPPED: Link with text '" + linkText + "' timed out after 5 seconds (" + href + ")." + RESET);
            counts[2]++; // Increment skipped count
            return "SKIPPED";
        } catch (Exception e) {
            System.out.println(RED + "FAILED: Link with text '" + linkText + "' could not be validated. Error: " + e.getMessage() + RESET);
            brokenLinks.add("Text: " + linkText + ", URL: " + href + " (Error: " + e.getMessage() + ")");
            counts[1]++; // Increment failed count
            return "FAILED";
        }
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

    public List<String> getBrokenLinks() {
        return brokenLinks;
    }
}
