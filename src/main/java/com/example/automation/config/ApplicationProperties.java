package com.example.automation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class ApplicationProperties {
    private String browser;
    private String baseUrl;
    private int defaultWaitTimeout;
}
