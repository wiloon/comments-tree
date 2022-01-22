package com.wiloon.comments.security;

import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.servlet.http.HttpServletRequest;

public class DefaultRememberMeService extends PersistentTokenBasedRememberMeServices {
    public DefaultRememberMeService(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
    }

    private boolean alwaysRemember;

    @Override
    protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
        if (alwaysRemember) {
            return true;
        }
        if (request != null && request.getMethod().equalsIgnoreCase("POST") && request.getContentType() != null &&
                (request.getContentType().equalsIgnoreCase(MediaType.APPLICATION_JSON_UTF8_VALUE) || request.getContentType().equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE))) {

            Boolean rememberMe = (Boolean) request.getAttribute("rememberMe");
            if (rememberMe) {
                return true;
            }
        }
        return super.rememberMeRequested(request, parameter);
    }

    @Override
    public void setAlwaysRemember(boolean alwaysRemember) {
        this.alwaysRemember = alwaysRemember;
    }
}
