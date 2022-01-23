package com.wiloon.comments.security;

import com.wiloon.comments.comment.CommentController;
import com.wiloon.comments.user.CommentsTreeUserDetails;
import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultRememberMeSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    @Autowired
    UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("remember me auth success");
        CommentsTreeUserDetails commentsTreeUserDetails = (CommentsTreeUserDetails) userService.loadUserByUsername(authentication.getName());
        request.getSession().setAttribute(User.SESSION_USER_ID_KEY, commentsTreeUserDetails.getUserId());
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
