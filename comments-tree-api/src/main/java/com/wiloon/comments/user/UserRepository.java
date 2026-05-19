package com.wiloon.comments.user;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, String> {

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(*) FROM users WHERE name = :name OR email = :email")
    long countByNameOrEmail(@Param("name") String name, @Param("email") String email);
}
