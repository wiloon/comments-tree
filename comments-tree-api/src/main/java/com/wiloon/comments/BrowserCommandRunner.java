package com.wiloon.comments;

import com.wiloon.comments.common.browser.Browser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * spring boot 容器加载后自动监听
 */
@Component
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
            System.out.println("自动加载指定的页面");
            try {
                browser.run(loginUrl);
                // Runtime.getRuntime().exec("cmd /c start " + loginUrl);
                // Runtime.getRuntime().exec(browserCommand + loginUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("浏览器打开页面异常");
            }
        }
    }

}
