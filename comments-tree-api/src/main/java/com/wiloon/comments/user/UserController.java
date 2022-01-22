package com.wiloon.comments.user;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import com.wiloon.comments.common.Utils;
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
    public String ping() {
        logger.info("ping");
        return "pong";
    }

//    /**
//     * 用户登录
//     */
//    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    @ResponseBody
//    public String login(@RequestBody JSONObject jsonParam, HttpSession session) {
//        logger.info("params: {}", jsonParam.toStringPretty());
//        String nameOrEmail = jsonParam.getStr("username");
//
//        User user;
//        if (Utils.isEmail(nameOrEmail)) {
//            user = userService.getUserByEmail(nameOrEmail);
//        } else {
//            user = userService.getUserByName(nameOrEmail);
//        }
//
//        if (user == null) {
//            return JSON.toJSONString(CommonResult.failed("用户不存在"));
//        } else if (user.isPasswordMatch(jsonParam.getStr("password"))) {
//            setAuthentication(user);
//            logger.info("login by user name success: {}", user);
//            session.setAttribute("userId", user.getId());
//            return JSON.toJSONString(CommonResult.success("登录成功"));
//        } else {
//            return JSON.toJSONString(CommonResult.failed("登录失败"));
//        }
//    }

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
        session.setAttribute("userId", user.getId());
        return JSON.toJSONString(CommonResult.success("注册成功"));
    }

    private void setAuthentication(User user) {
        CommentsTreeUserDetails commentsTreeUserDetails = new CommentsTreeUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(commentsTreeUserDetails, null, commentsTreeUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("login by user name success: {}", user);
    }

    @RequestMapping(value = "/session", method = RequestMethod.GET)
    @ResponseBody
    public String sessionCheck() {
        return JSON.toJSONString(CommonResult.success(""));
    }
}
