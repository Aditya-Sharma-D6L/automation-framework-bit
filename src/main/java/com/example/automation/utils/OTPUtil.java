package com.example.automation.utils;

import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import java.util.Map;

public class OTPUtil {

    public static String generatedOTP;

    public static String generateTOTPForUser(String username) {

        Map<String, String> userSecretKeys = Map.of(
//                "copt1@yopmail.com", "EVQXG63WEZKHM2L5GNVUG5DBJM5FGWBO",
                "copt1@yopmail.com", "HZTEU5LTMFRG6PTTFQQT6ZBZHZ2FMORU"
        );

        if (userSecretKeys.containsKey(username)) {
            System.out.println("\n PASS");
        } else {
            throw new IllegalArgumentException("INVALID NAME");
        }
        String secretKey = userSecretKeys.get(username);
        if (secretKey == null) {
            throw new IllegalArgumentException("No secret key found for the given username: " + username);
        }

        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        String otp = TOTP.getOTP(hexKey);
        System.out.println("The OTP is " + otp);

        generatedOTP = otp;
        return otp;
    }

//    public static void main(String... a) {
//        generateTOTPForUser("user50usd@mailinator.com");
//
//    }

}

