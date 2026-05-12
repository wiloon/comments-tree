package com.wiloon.comments;

import com.wiloon.comments.common.browser.Browser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Auto-runs after Spring Boot container starts
 */
// @Component
@Slf4j
public class BrowserCommandRunner implements CommandLineRunner {
    @Value("${spring.web.loginurl}")
    private String loginUrl;

    @Value("${spring.auto.openurl}")
    private boolean isOpen;

    private String browserCommand;

    @Autowired
    Browser browser;

    @Override
    public void run(String... args) {
        if (isOpen) {
            log.info("invoke browser and open: {}", loginUrl);
            try {
                browser.run(loginUrl);
            } catch (Exception ex) {
                log.error("failed to invoke browser, url: {}", loginUrl, ex);
            }
        }
    }

}
