package com.example.automation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "")
@Data
public class ApplicationProperties {
    private String browser;
    private String baseUrl;
    private boolean headless;
    private int defaultWaitTimeout;
}
