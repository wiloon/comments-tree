package com.wiloon.comments.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户
 * @author wiloon
 */
public class User {
    public static final String ANONYMOUS_USER="anonymousUser";
    private String id;
    @NotBlank
    @Size(min = 5, max = 20)
    private String name;
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 8, max = 20)
    private transient String password;

    public User() {

    }

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
}