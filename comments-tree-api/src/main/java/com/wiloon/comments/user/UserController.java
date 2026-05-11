package com.wiloon.comments.user;

import com.wiloon.comments.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户 controller
 *
 * @author wiloon
 */
@RestController
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public CommonResult<String> register(@RequestBody User params) {
        logger.info("user register, params: {}", params);
        String name = params.getName();
        String password = params.getPassword();

        if (userService.isUserRegistered(name, params.getEmail())) {
            return CommonResult.failed("用户已存在");
        }
        String id = userService.userRegister(name, params.getEmail(), password);
        if (id == null || "".equals(id)) {
            return CommonResult.failed("用户注册失败");
        }
        return CommonResult.success("注册成功, 请登录。");
    }

    /**
     * 检查session是否有效并返回用户信息, session过期时会被 spring security 拦截掉 返回 状态码401
     *
     * @return 用户信息
     */
    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public CommonResult sessionCheck() {
        logger.info("session check");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || User.ANONYMOUS_USER.equals(authentication.getName())) {
            return CommonResult.unauthorized("用户未登录");
        } else {
            logger.info("session check, user name: {}", authentication.getName());
            User user = userService.getUserByNameOrEmail(authentication.getName());
            return CommonResult.success(user);
        }
    }
}
