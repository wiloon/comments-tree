package com.wiloon.comments.common.browser;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LinuxBrowser extends Browser {
    public LinuxBrowser() {
    }

    @Override
    public void run(String url) throws IOException {
        try {
            String[] browsers = {"chromium", "google-chrome-stable", "firefox", "google-chrome-beta"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++) {
                log.info("check browser: {}", browsers[count]);
                if (Runtime.getRuntime()
                        .exec(new String[]{"which", browsers[count]})
                        .waitFor() == 0)
                    browser = browsers[count];
                log.info("check browser: {}", browser);
            }
            if (browser == null)
                throw new Exception("Could not find web browser");
            else
                Runtime.getRuntime().exec(new String[]{browser, url});
        } catch (Exception e) {
            log.error("failed to open url: {}", url, e);
        }
    }
}
