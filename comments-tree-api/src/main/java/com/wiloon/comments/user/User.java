package com.wiloon.comments.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class User {
    private static final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
    private String id;
    private String name;
    private String email;
    private String password;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("User, id: %s, name: %s, password: %s", this.id, this.name, this.password);
    }

    public boolean isPasswordMatch(String password) {
        return bcryptPasswordEncoder.matches(password, this.getPassword());
    }
}