package com.wiloon.comments.user;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    public String register(@RequestBody User params) {
        logger.info("user register, params: {}", params);
        String name = params.getName();
        String password = params.getPassword();
        if (StringUtils.isBlank(name) || name.length() < 5 || name.length() > 20) {
            return JSON.toJSONString(CommonResult.failed("无效的用户名"));
        }

        if (StringUtils.isBlank(password) || password.length()< 8 || password.length() > 20){
            return JSON.toJSONString(CommonResult.failed("无效的密码"));
        }

        if (userService.isUserRegistered(name, params.getEmail())) {
            return JSON.toJSONString(CommonResult.failed("用户已存在"));
        }
        String id = userService.userRegister(name, params.getEmail(), password);
        if (id == null || "".equals(id)) {
            return JSON.toJSONString(CommonResult.failed("用户注册失败"));
        }
        return JSON.toJSONString(CommonResult.success("注册成功, 请登录。"));
    }

    /**
     * 检查session是否有效并返回用户信息, session过期时会被 spring security 拦截掉 返回 状态码401
     *
     * @return 用户信息
     */
    @RequestMapping(value = "/session", method = RequestMethod.GET)
    @ResponseBody
    public String sessionCheck() {
        logger.info("session check");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return JSON.toJSONString(CommonResult.unauthorized("用户未登录"));
        } else {
            logger.info("session check, user name: {}", authentication.getName());
            User user = userService.getUserByNameOrEmail(authentication.getName());
            return JSON.toJSONString(CommonResult.success(user));
        }
    }
}
