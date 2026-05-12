package com.wiloon.comments.common.browser;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Browser {
    protected String command;

    public Browser() {

    }

    public Browser(String command) {
        this.command = command;
    }

    public void run(String url) throws IOException {
        log.info("open url: {}", url);
        Runtime.getRuntime().exec(new String[]{command, url});
    }
}
