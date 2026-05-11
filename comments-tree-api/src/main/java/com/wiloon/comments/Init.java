package com.wiloon.comments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * init sqlite db
 */
@Component
public class Init implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(Init.class);

    private final JdbcTemplate jdbcTemplate;

    public Init(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("application started, init sqlite tables");

        jdbcTemplate.execute((ConnectionCallback<Object>) con -> {
            con.setAutoCommit(false);
            ClassPathResource classPathResource = new ClassPathResource("jdbc/schema.sql");
            EncodedResource encodedResource = new EncodedResource(classPathResource, "utf-8");
            ScriptUtils.executeSqlScript(con, encodedResource);
            con.commit();
            logger.info("sql executed: {}", classPathResource.getPath());
            return null;
        });
    }
}
