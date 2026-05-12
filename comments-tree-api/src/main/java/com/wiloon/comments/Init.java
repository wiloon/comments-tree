package com.wiloon.comments;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class Init implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public Init(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("application started, init sqlite tables");

        jdbcTemplate.execute((ConnectionCallback<Object>) con -> {
            con.setAutoCommit(false);
            ClassPathResource classPathResource = new ClassPathResource("jdbc/schema.sql");
            EncodedResource encodedResource = new EncodedResource(classPathResource, "utf-8");
            ScriptUtils.executeSqlScript(con, encodedResource);
            con.commit();
            log.info("sql executed: {}", classPathResource.getPath());
            return null;
        });
    }
}
