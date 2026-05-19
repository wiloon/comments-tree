package com.wiloon.comments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Initialize SQLite schema on startup
 */
@Slf4j
@Component
public class Init implements ApplicationRunner {

    private final DataSource dataSource;

    public Init(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("application started, init sqlite tables");
        ClassPathResource classPathResource = new ClassPathResource("jdbc/schema.sql");
        EncodedResource encodedResource = new EncodedResource(classPathResource, "utf-8");
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            ScriptUtils.executeSqlScript(connection, encodedResource);
            connection.commit();
            log.info("sql executed: {}", classPathResource.getPath());
        } catch (Exception e) {
            throw new IllegalStateException("failed to initialize database schema", e);
        }
    }
}
