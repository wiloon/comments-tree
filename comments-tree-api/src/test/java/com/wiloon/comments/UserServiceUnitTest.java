package com.wiloon.comments;

import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserRepository;
import com.wiloon.comments.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * UserService unit tests: constructor injection, empty lookup handling, PasswordEncoder delegation
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void getUserByName_notFound_returnsNull() {
        when(userRepository.findByName("nobody")).thenReturn(Optional.empty());

        User result = userService.getUserByName("nobody");

        assertNull(result);
    }

    @Test
    public void getUserByEmail_notFound_returnsNull() {
        when(userRepository.findByEmail("no@body.com")).thenReturn(Optional.empty());

        User result = userService.getUserByEmail("no@body.com");

        assertNull(result);
    }

    @Test
    public void getUserById_notFound_returnsNull() {
        when(userRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        User result = userService.getUserById("nonexistent-id");

        assertNull(result);
    }

    @Test
    public void hashPassword_delegatesToInjectedEncoder() {
        when(passwordEncoder.encode("mySecret")).thenReturn("hashed_value");

        String result = userService.hashPassword("mySecret");

        assertEquals("hashed_value", result);
    }

    @Test
    public void isUserRegistered_returnsTrueWhenExists() {
        when(userRepository.countByNameOrEmail("alice", "alice@example.com")).thenReturn(1L);

        assertTrue(userService.isUserRegistered("alice", "alice@example.com"));
    }

    @Test
    public void isUserRegistered_returnsFalseWhenNotExists() {
        when(userRepository.countByNameOrEmail("newuser", "new@example.com")).thenReturn(0L);

        assertFalse(userService.isUserRegistered("newuser", "new@example.com"));
    }

    @Test
    public void loadUserByUsername_notFound_throwsException() {
        when(userRepository.findByName("ghost")).thenReturn(Optional.empty());

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("ghost"));
    }
}
