package com.example.automation.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.util.Timeout;

@Slf4j
public class HttpStatusCodeChecker {

    /**
     * Checks the HTTP status code for a given URL.
     *
     * @param url The URL to check.
     * @return The HTTP status code.
     * @throws Exception If the request fails or times out.
     */
    public static int checkStatusCode(String url) throws Exception {
        try {
            return Request.get(url)
                    .connectTimeout(Timeout.ofSeconds(5))
                    .responseTimeout(Timeout.ofSeconds(5))
                    .execute()
                    .returnResponse()
                    .getCode();
        } catch (Exception e) {
            throw new Exception("Error while checking status code for URL: " + url + ". Error: " + e.getMessage());
        }
    }

}
