package com.wiloon.p219;

import com.wiloon.p219.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AppController {
    private static final Logger logger = LoggerFactory.getLogger(AppController.class);
    @Autowired
    UserService userService;

    @RequestMapping("/ping")
    @ResponseBody
    public String ping() {
        return "pong";
    }

    @PostMapping("/register")
    @ResponseBody
    public String register(@RequestParam("name") String name,
                           @RequestParam("name") String email,
                           @RequestParam("name") String password) {
        userService.createUser(name,email,password);
        return "pong";
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(@PathVariable("name") String name,
                        @PathVariable("password") String password) {
        boolean result=userService.loginByUserName(name,password);
        logger.info("login: {}",result);
        return "pong";
    }

    @RequestMapping("/get")
    @ResponseBody
    public String get() {

//        //Read records:
//        List<User> beers = jdbcTemplate.query("SELECT * FROM beers",
//                (resultSet, rowNum) -> new User(resultSet.getString("name")));
//
//        //Print read records:
//        beers.forEach(System.out::println);
        return "pong";
    }
}
