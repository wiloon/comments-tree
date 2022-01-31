package com.wiloon.comments.security;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JSON Login success response
 * @author wiloon
 */
public class DefaultAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        logger.debug("name: {}", authentication.getName());

        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(JSON.toJSONString(CommonResult.success("登录成功")));
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
    }
}
