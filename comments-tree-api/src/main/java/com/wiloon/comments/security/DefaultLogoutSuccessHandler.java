package com.wiloon.comments.security;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * restful logout
 * @author wiloon
 */
public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(JSON.toJSONString(CommonResult.success("退出成功")));
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
    }
}
