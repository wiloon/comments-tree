package com.wiloon.comments.user;

import com.wiloon.comments.common.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByName(String name) {
        return userRepository.findByName(name).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean isUserRegistered(String name, String email) {
        return userRepository.countByNameOrEmail(name, email) > 0;
    }

    public String userRegister(String name, String email, String password) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(hashPassword(password));
        user.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return userRepository.save(user).getId();
    }

    @Override
    public UserDetails loadUserByUsername(String nameOrEmail) throws UsernameNotFoundException {
        log.info("loadUserByUsername: {}", nameOrEmail);
        User user = getUserByNameOrEmail(nameOrEmail);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + nameOrEmail);
        }
        return new CommentsTreeUserDetails(user);
    }

    public User getUserByNameOrEmail(String nameOrEmail) {
        if (Utils.isEmail(nameOrEmail)) {
            return getUserByEmail(nameOrEmail);
        }
        return getUserByName(nameOrEmail);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
