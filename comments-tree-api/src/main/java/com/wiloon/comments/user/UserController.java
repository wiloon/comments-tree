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

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ResponseBody
    public String ping(@RequestParam(value = "key", required = false) String key) {
        logger.info("key:{}", key);
        return "pong";
    }

    @RequestMapping("/bar")
    @ResponseBody
    public String get() {
        return "pong";
    }

    @PostMapping("/user/register")
    @ResponseBody
    public String register(@RequestParam("name") String name,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password) {
        userService.createUser(name, email, password);
        return "pong";
    }

    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestBody JSONObject jsonParam, HttpSession session) {
        logger.info("params: {}", jsonParam.toStringPretty());
        User user = userService.getUserByName(jsonParam.getStr("name"));

        if (user == null) {
            return JSON.toJSONString(CommonResult.failed("用户不存在"));
        } else if (user.isPasswordMatch(jsonParam.getStr("password"))) {
            AdminUserDetails adminUserDetails = new AdminUserDetails(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(adminUserDetails, null, adminUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("login by user name success: {}", user);
            session.setAttribute("userId", user.getId());
            return JSON.toJSONString(CommonResult.success("登录成功"));
        } else {
            return JSON.toJSONString(CommonResult.failed("登录失败"));
        }
    }
}
