package com.wiloon.comments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Unlimited nested comments
 * @author wiloon
 */
@Slf4j
@SpringBootApplication
@EnableJdbcRepositories
public class CommentsTree {
    public static void main(String[] args) {
        log.info("comments tree starting...");
        SpringApplication.run(CommentsTree.class);
    }
}
