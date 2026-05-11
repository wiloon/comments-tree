package com.wiloon.comments;

import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserRowMapper;
import com.wiloon.comments.user.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * UserService 单元测试
 * 验证：构造器注入、queryForObject 空结果处理、PasswordEncoder 注入
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceUnitTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // --- queryForObject 空结果处理 ---

    @Test
    public void getUserByName_notFound_returnsNull() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq("nobody")))
                .thenThrow(new EmptyResultDataAccessException(1));

        User result = userService.getUserByName("nobody");

        assertNull(result);
    }

    @Test
    public void getUserByEmail_notFound_returnsNull() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq("no@body.com")))
                .thenThrow(new EmptyResultDataAccessException(1));

        User result = userService.getUserByEmail("no@body.com");

        assertNull(result);
    }

    @Test
    public void getUserById_notFound_returnsNull() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq("nonexistent-id")))
                .thenThrow(new EmptyResultDataAccessException(1));

        User result = userService.getUserById("nonexistent-id");

        assertNull(result);
    }

    // --- PasswordEncoder 注入验证（不再静态初始化）---

    @Test
    public void hashPassword_delegatesToInjectedEncoder() {
        when(passwordEncoder.encode("mySecret")).thenReturn("hashed_value");

        String result = userService.hashPassword("mySecret");

        assertEquals("hashed_value", result);
    }

    // --- isUserRegistered ---

    @Test
    public void isUserRegistered_returnsTrueWhenExists() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("alice"), eq("alice@example.com")))
                .thenReturn(1);

        assertTrue(userService.isUserRegistered("alice", "alice@example.com"));
    }

    @Test
    public void isUserRegistered_returnsFalseWhenNotExists() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("newuser"), eq("new@example.com")))
                .thenReturn(0);

        assertFalse(userService.isUserRegistered("newuser", "new@example.com"));
    }

    // --- loadUserByUsername 用户不存在时抛出 UsernameNotFoundException ---

    @Test(expected = org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public void loadUserByUsername_notFound_throwsException() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq("ghost")))
                .thenThrow(new EmptyResultDataAccessException(1));

        userService.loadUserByUsername("ghost");
    }
}
