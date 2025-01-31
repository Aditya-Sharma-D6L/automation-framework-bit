package com.example.automation.utilities;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Callable;

@Slf4j
public class Retry {

    public static <T> void retryOperation(Callable<T> operation,
                                          int maxRetries,
                                          long retryDelayMs,
                                          String operationName) throws Exception {
        int attempt = 0;
        Exception lastError = null;

        while (attempt <= maxRetries) {
            try {
                log.info("Attempt {} of {} for {}", attempt + 1, maxRetries, operationName);
                operation.call();
                return;
            } catch (Exception e) {
                lastError = e;
                log.warn("Attempt {} failed: {}", attempt + 1, e.getMessage());

                if (attempt < maxRetries) {
                    Thread.sleep(retryDelayMs);
                }
                attempt++;
            }
        }
        throw new Exception("Operation '" + operationName + "' failed after " + maxRetries + " attempts", lastError);
    }
}