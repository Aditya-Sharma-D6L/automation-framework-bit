package com.example.automation.utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import lombok.Getter;

public class ExtentReportManager {

    private static ExtentReports extent;
    @Getter
    private static ExtentTest test;

    /**
     * Initializes the ExtentReports instance.
     */
    public static ExtentReports getExtentReports() {
        if (extent == null) {
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter("report/ExtentReport.html");
            sparkReporter.config().setReportName("Automation Test Report");
            sparkReporter.config().setDocumentTitle("Test Execution Report");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("Operating System", System.getProperty("os.name"));
            extent.setSystemInfo("Environment", System.getProperty("env", "qa"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
        }
        return extent;
    }

    /**
     * Starts a test in the Extent Report.
     */
    public static ExtentTest createTest(String testName, String description) {
        test = getExtentReports().createTest(testName, description);
        return test;
    }

    /**
     * Flushes the Extent Report to write the results.
     */
    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}
