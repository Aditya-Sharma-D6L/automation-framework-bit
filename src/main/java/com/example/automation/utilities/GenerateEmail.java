package com.example.automation.utilities;

import com.example.automation.config.Config;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class GenerateEmail {

    /**
     * Generates a dynamic email for testing.
     * @return A unique email address.
     */
    public String generateEmail() {

        String baseEmail = "automation@yopmail.com";
        String alias = Config.getEnvironment();

        // Get current time in MMss format
        String currentTime = new SimpleDateFormat("mmss").format(new Date());

        // Split the base email into username and domain
        String[] emailParts = baseEmail.split("@");

        // Create new email by appending minutes and seconds to the username
        return emailParts[0] + alias + currentTime + "@" + emailParts[1];
    }

}
