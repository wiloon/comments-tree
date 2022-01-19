package com.wiloon.p219.user;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.wiloon.p219.common.CommonResult;
import com.wiloon.p219.comment.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "/foo", method = RequestMethod.POST)
    @ResponseBody
    public String foo(@RequestParam(value = "key", required = true) String key) {
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
    public String login(@RequestBody JSONObject jsonParam) {
        logger.info("params: {}", jsonParam.toStringPretty());

        boolean result = userService.loginByUserName(jsonParam.getStr("name"), jsonParam.getStr("password"));
        logger.info("login: {}", result);
        return JSON.toJSONString(CommonResult.success("login"));
    }

    @RequestMapping(value = "/message/save", method = RequestMethod.POST)
    @ResponseBody
    public String messageSave(@RequestBody JSONObject jsonParam) {
        logger.info("messageSave params: {}", jsonParam.toStringPretty());

        return JSON.toJSONString(CommonResult.success("msg save"));
    }
}
