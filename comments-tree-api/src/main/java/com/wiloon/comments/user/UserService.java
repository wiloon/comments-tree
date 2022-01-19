package com.wiloon.comments.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();

    public void createUser(String name, String email, String password) {
        User user = new User(name);
        user.setEmail(email);

        String hashPass = bcryptPasswordEncoder.encode(password);

        user.setPassword(hashPass);
        user.setId(UUID.randomUUID().toString());
        jdbcTemplate.execute("INSERT INTO beers VALUES ('Stella')");
        logger.info("user created: {}", user);
    }

    public boolean loginByUserName(String name, String password) {
        boolean result;
        User user = jdbcTemplate.queryForObject("SELECT * FROM users where name=?", new UserRowMapper(), name);
        logger.info("find user by name: {}", user);

        result = bcryptPasswordEncoder.matches(password, user.getPassword());
        if (result) {
            AdminUserDetails adminUserDetails = new AdminUserDetails(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(adminUserDetails, null, adminUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("login by user name success: {}", user);
        }
        return result;
    }

    public String hashPassword(String password) {
        return bcryptPasswordEncoder.encode(password);
    }

    public boolean isPasswordMatch(String password, String hash) {
        return bcryptPasswordEncoder.matches(password, hash);
    }
}
