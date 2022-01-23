package com.wiloon.comments.common.browser;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrowserConfig {
    @Bean("browser")
    @Conditional(LinuxEnvironmentCondition.class)
    public Browser linuxBrowserCommand() {
        return new LinuxBrowser();
    }

    @Bean("browser")
    @Conditional(WindowsEnvironmentCondition.class)
    public Browser windowsBrowserCommand() {
        return new Browser("rundll32 url.dll,FileProtocolHandler");
    }

    @Bean("browser")
    @Conditional(WindowsEnvironmentCondition.class)
    public Browser macOsBrowserCommand() {
        return new MacOsBrowser();
    }
}
