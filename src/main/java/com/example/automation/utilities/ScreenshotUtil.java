package com.example.automation.utilities;

import com.aventstack.extentreports.ExtentTest;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class ScreenshotUtil {

    /**
     * Captures a screenshot and attaches it to the provided ExtentTest instance.
     *
     * @param driver    The WebDriver instance.
     * @param test      The ExtentTest instance for reporting.
     * @param testName  The name of the test for file naming.
     */
    public static void captureScreenshot(WebDriver driver, ExtentTest test, String testName) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String path = "report/screenshots/" + testName + ".png";
            Files.createDirectories(new File("report/screenshots").toPath()); // Ensure directory exists
            Files.copy(screenshot.toPath(), new File(path).toPath());
            test.addScreenCaptureFromPath(path);
        } catch (IOException e) {
            log.error("Failed to capture screenshot for test: {}", testName, e);
        }
    }
}
