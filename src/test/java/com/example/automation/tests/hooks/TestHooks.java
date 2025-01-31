// TestHooks.java
package com.example.automation.tests.hooks;

import com.example.automation.config.Config;
import com.example.automation.drivers.DriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@Slf4j
@Component
public class TestHooks {

    @Autowired
    private DriverManager driverManager;

    @PostConstruct
    public void logActiveProfile() {
        String activeProfile = System.getProperty("spring.profiles.active");
        log.info("Active Profile: {}", activeProfile);
        Config.setEnvironment();
        log.info("Current Environment: {}", Config.getEnvironment());
    }

    @AfterMethod
    public void tearDown() {
        log.info("Quitting WebDriver after the test...");
        driverManager.quitDriver();
    }
}