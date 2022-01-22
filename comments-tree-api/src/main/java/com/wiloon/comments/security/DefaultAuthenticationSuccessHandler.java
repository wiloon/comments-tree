package com.wiloon.comments.security;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.comment.CommentController;
import com.wiloon.comments.common.CommonResult;
import com.wiloon.comments.user.CommentsTreeUserDetails;
import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAuthenticationSuccessHandler.class);
    @Autowired
    UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.debug("name: {}", authentication.getName());
        CommentsTreeUserDetails user = (CommentsTreeUserDetails) userService.loadUserByUsername(authentication.getName());
        request.getSession().setAttribute("userId", user.getUserId());

        String accessControlAllowOrigin = request.getHeader("Access-Control-Allow-Origin");

        response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
        response.addHeader("Access-Control-Allow-Credentials", "true");

        response.setContentType("text/json;charset=UTF-8");
        response.getWriter().println(JSON.toJSONString(CommonResult.success("登录成功")));
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
    }
}
