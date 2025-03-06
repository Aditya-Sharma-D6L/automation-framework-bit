package com.example.automation.utils;

import com.aventstack.extentreports.ExtentTest;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class ScreenshotUtil {

    /**
     * ✅ Captures a screenshot and attaches it to the Extent Report.
     *
     * @param driver   WebDriver instance
     * @param test     ExtentTest instance for logging
     * @param testName Name of the test method
     */
    public static void captureScreenshot(WebDriver driver, ExtentTest test, String testName) {
        try {
            // ✅ Get timestamp to avoid overwriting screenshots
            String timestamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());

            // ✅ Define screenshot directory
            Path screenshotDir = Path.of("report/screenshots");
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir); // ✅ Ensure the folder exists
            }

            // ✅ Define the full path for the screenshot file (with timestamp)
            String screenshotFileName = testName + "_" + timestamp + ".png";
            Path screenshotPath = screenshotDir.resolve(screenshotFileName);

            // ✅ Capture the screenshot
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // ✅ Save the screenshot with a unique name
            Files.copy(screenshot.toPath(), screenshotPath, StandardCopyOption.REPLACE_EXISTING);

            // ✅ Attach the screenshot to the Extent Report
            test.info("Screenshot captured: " + screenshotPath.toAbsolutePath());
            test.addScreenCaptureFromPath(screenshotPath.toString()); // ✅ Add Screenshot to Extent Report

            log.info("Screenshot saved at: {}", screenshotPath.toAbsolutePath());
        } catch (IOException e) {
            test.fail("Failed to capture screenshot: " + e.getMessage());
            log.error("Failed to save screenshot", e);
        }
    }
}
