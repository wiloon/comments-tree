package com.wiloon.comments.common.browser;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MacOsBrowser extends Browser {
    public MacOsBrowser() {
    }

    @Override
    public void run(String url) throws IOException {
        try {
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
            openURL.invoke(null, new Object[]{url});
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("failed to open url on macOS: {}", url, e);
        }
    }
}
