package com.wiloon.comments.security;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * restful access denied handler
 * @author wiloon
 */
public class RestfulAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestfulAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        logger.debug("restful access denied handler.");
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(JSON.toJSONString(CommonResult.forbidden(e.getMessage())));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().flush();
    }
}
