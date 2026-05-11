package com.wiloon.comments.user;

import com.wiloon.comments.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByName(String name) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users where name=?", new UserRowMapper(), name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User getUserByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users where email=?", new UserRowMapper(), email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User getUserById(String id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users where id=?", new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean isUserRegistered(String name, String email) {
        String sql = "SELECT count(*) FROM users WHERE NAME=? OR email=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, email);
        return count != null && count > 0;
    }

    public String userRegister(String name, String email, String password) {
        String sql = "INSERT INTO users (id,name,email,password,create_time) VALUES (?,?,?,?,?);";
        String id = UUID.randomUUID().toString();
        int result = jdbcTemplate.update(sql, id, name, email, hashPassword(password), new Timestamp(System.currentTimeMillis()));
        return result > 0 ? id : "";
    }

    @Override
    public UserDetails loadUserByUsername(String nameOrEmail) throws UsernameNotFoundException {
        logger.info("loadUserByUsername: {}", nameOrEmail);
        User user = getUserByNameOrEmail(nameOrEmail);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + nameOrEmail);
        }
        return new CommentsTreeUserDetails(user);
    }

    public User getUserByNameOrEmail(String nameOrEmail) {
        if (Utils.isEmail(nameOrEmail)) {
            return getUserByEmail(nameOrEmail);
        } else {
            return getUserByName(nameOrEmail);
        }
    }

    public void deleteUser(String id) {
        String sql = "delete from  users  where id=?";
        jdbcTemplate.update(sql, id);
    }
}
