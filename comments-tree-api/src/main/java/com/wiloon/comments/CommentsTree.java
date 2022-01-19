package com.wiloon.comments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 无限层级留言
 */
@SpringBootApplication
public class CommentsTree {
    private static final Logger logger = LoggerFactory.getLogger(CommentsTree.class);

    public static void main(String[] args) {
        logger.info("comments tree starting...");
        SpringApplication.run(CommentsTree.class);
    }
}
