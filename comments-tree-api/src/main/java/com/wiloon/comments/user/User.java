package com.wiloon.comments.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

/**
 * User entity
 *
 * @author wiloon
 */
@Table("users")
public class User implements Persistable<String> {

    @Transient
    private boolean newEntity = true;
    public static final String ANONYMOUS_USER = "anonymousUser";

    @Id
    @Column("id")
    private String id;

    @NotBlank
    @Size(min = 5, max = 20)
    @Column("name")
    private String name;

    @NotBlank 
    @Column("email")
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    @Column("password")
    private String password;

    @Column("create_time")
    private Timestamp createTime;

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

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    public void setNewEntity(boolean newEntity) {
        this.newEntity = newEntity;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return String.format("User, id: %s, name: %s, password: %s", this.id, this.name, this.password);
    }
}