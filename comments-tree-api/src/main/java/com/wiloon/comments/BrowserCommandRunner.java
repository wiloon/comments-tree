package com.wiloon.comments;

import com.wiloon.comments.common.browser.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * spring boot 容器加载后自动监听
 */
// @Component
public class BrowserCommandRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(BrowserCommandRunner.class);
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
            logger.info("invoke browser and open: {}", loginUrl);
            try {
                browser.run(loginUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error("failed to invoke browser, url: {}", loginUrl);
            }
        }
    }

}
