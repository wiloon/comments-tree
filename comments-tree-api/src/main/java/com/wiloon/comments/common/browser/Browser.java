package com.wiloon.comments.common.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Browser {
    private static final Logger logger = LoggerFactory.getLogger(Browser.class);
    protected String command;

    public Browser() {

    }

    public Browser(String command) {
        this.command = command;
    }

    public void run(String url) throws IOException {
        logger.info("open url: {}", url);
        Runtime.getRuntime().exec(new String[]{command, url});
    }
}
