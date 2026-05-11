package com.wiloon.comments.common.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MacOsBrowser extends Browser {
    private static final Logger logger = LoggerFactory.getLogger(MacOsBrowser.class);

    public MacOsBrowser() {
    }

    @Override
    public void run(String url) throws IOException {
        try {
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
            openURL.invoke(null, new Object[]{url});
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            logger.error("failed to open url on macOS: {}", url, e);
        }
    }
}
