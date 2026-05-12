package com.wiloon.comments.user;

import com.wiloon.comments.common.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * User controller
 *
 * @author wiloon
 */
@Slf4j
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * User registration
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public CommonResult<String> register(@RequestBody User params) {
        log.info("user register, params: {}", params);
        String name = params.getName();
        String password = params.getPassword();

        if (userService.isUserRegistered(name, params.getEmail())) {
            return CommonResult.failed("User already exists");
        }
        String id = userService.userRegister(name, params.getEmail(), password);
        if (id == null || "".equals(id)) {
            return CommonResult.failed("User registration failed");
        }
        return CommonResult.success("Registration successful, please log in.");
    }

    /**
     * Check if the session is valid and return user info; expired sessions are intercepted by Spring Security with 401
     *
     * @return user info
     */
    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public CommonResult sessionCheck() {
        log.info("session check");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || User.ANONYMOUS_USER.equals(authentication.getName())) {
            return CommonResult.unauthorized("User not logged in");
        } else {
            log.info("session check, user name: {}", authentication.getName());
            User user = userService.getUserByNameOrEmail(authentication.getName());
            return CommonResult.success(user);
        }
    }
}
