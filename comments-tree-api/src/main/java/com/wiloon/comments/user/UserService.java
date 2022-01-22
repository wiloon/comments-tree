package com.wiloon.comments.user;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import com.wiloon.comments.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();

    public User getUserByName(String name) {
        return jdbcTemplate.queryForObject("SELECT * FROM users where name=?", new UserRowMapper(), name);
    }
    public User getUserByEmail(String email) {
        return jdbcTemplate.queryForObject("SELECT * FROM users where email=?", new UserRowMapper(), email);
    }
    public User getUserById(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM users where id=?", new UserRowMapper(), id);
    }
    public String hashPassword(String password) {
        return bcryptPasswordEncoder.encode(password);
    }

    public boolean isPasswordMatch(String password, String hash) {
        return bcryptPasswordEncoder.matches(password, hash);
    }

    public boolean isUserRegistered(String name, String email) {
        String sql = "SELECT count(*) FROM users WHERE NAME=? OR email=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, email);
        return count != null && count > 0;
    }

    public String userRegister(String name, String email, String password) {
        String sql = "INSERT INTO users (id,name,email,password) VALUES (?,?,?,?);";
        String id = UUID.randomUUID().toString();
        int result = jdbcTemplate.update(sql, id, name, email, hashPassword(password));
        return result > 0 ? id : "";
    }

    @Override
    public UserDetails loadUserByUsername(String nameOrEmail) throws UsernameNotFoundException {
        logger.info("loadUserByUsername: {}", nameOrEmail);
        User user;
        if (Utils.isEmail(nameOrEmail)) {
            user = getUserByEmail(nameOrEmail);
        } else {
            user = getUserByName(nameOrEmail);
        }
        return new CommentsTreeUserDetails(user);
    }
}
