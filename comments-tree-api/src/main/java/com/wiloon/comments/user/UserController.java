package com.wiloon.comments.user;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * 用户 controller
 */
@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    /**
     * 用户注册
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ResponseBody
    public String register(@RequestBody JSONObject jsonParam, HttpSession session) {
        logger.info("params: {}", jsonParam.toStringPretty());
        String name = jsonParam.getStr("name");
        String email = jsonParam.getStr("email");
        String password = jsonParam.getStr("password");
        if (userService.isUserRegistered(name, email)) {
            return JSON.toJSONString(CommonResult.failed("用户已存在"));
        }
        String id = userService.userRegister(name, email, password);
        if (id == null || "".equals(id)) {
            return JSON.toJSONString(CommonResult.failed("用户注册失败"));
        }
        User user = userService.getUserById(id);
        setAuthentication(user);
        session.setAttribute(User.SESSION_USER_ID_KEY, user.getId());
        return JSON.toJSONString(CommonResult.success("注册成功"));
    }

    private void setAuthentication(User user) {
        CommentsTreeUserDetails commentsTreeUserDetails = new CommentsTreeUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(commentsTreeUserDetails, null, commentsTreeUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("login by user name success: {}", user);
    }

    /**
     * 检查session是否有效并返回用户信息, session过期时会被 spring security 拦截掉 返回 状态码401
     *
     * @param session 用户 session
     * @return 用户信息
     */
    @RequestMapping(value = "/session", method = RequestMethod.GET)
    @ResponseBody
    public String sessionCheck(HttpSession session) {
        logger.info("session check");
        logger.info("session: {}", session);
        logger.info("userId: {}", session.getAttribute(User.SESSION_USER_ID_KEY));
        Object userId = session.getAttribute(User.SESSION_USER_ID_KEY);
        if (userId == null) {
            return JSON.toJSONString(CommonResult.unauthorized("用户未登录"));
        } else {
            User user = userService.getUserById((String) userId);
            return JSON.toJSONString(CommonResult.success(user));
        }

    }
}
