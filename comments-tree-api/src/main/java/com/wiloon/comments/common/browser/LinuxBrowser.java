package com.wiloon.comments.common.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LinuxBrowser extends Browser {
    private static final Logger logger = LoggerFactory.getLogger(LinuxBrowser.class);

    public LinuxBrowser() {
    }

    public void run(String url) throws IOException {
        try {
            String[] browsers = {"chromium", "google-chrome-stable", "firefox", "google-chrome-beta"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++) {
                logger.info("check browser: {}", browsers[count]);
                if (Runtime.getRuntime()
                        .exec(new String[]{"which", browsers[count]})
                        .waitFor() == 0)
                    browser = browsers[count];
                logger.info("check browser: {}", browser);
            }
            if (browser == null)
                throw new Exception("Could not find web browser");
            else
                Runtime.getRuntime().exec(new String[]{browser, url});
        } catch (Exception e) {
            logger.error("failed to open url: {}", url, e);
        }
    }
}
