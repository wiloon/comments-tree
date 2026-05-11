package com.wiloon.comments.common.browser;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrowserConfig {
    @Bean
    @Conditional(LinuxEnvironmentCondition.class)
    public Browser linuxBrowser() {
        return new LinuxBrowser();
    }

    @Bean
    @Conditional(WindowsEnvironmentCondition.class)
    public Browser windowsBrowser() {
        return new Browser("rundll32 url.dll,FileProtocolHandler");
    }

    @Bean
    @Conditional(MacOsEnvironmentCondition.class)
    public Browser macOsBrowser() {
        return new MacOsBrowser();
    }
}
