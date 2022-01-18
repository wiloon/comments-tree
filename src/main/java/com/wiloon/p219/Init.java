package com.wiloon.p219;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

@Component
public class Init implements ServletContextAware {
    private static final Logger logger = LoggerFactory.getLogger(Init.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void setServletContext(ServletContext servletContext) {
        logger.info("servlet context");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users(\n" +
                "  id char(36) PRIMARY KEY NOT NULL,\n" +
                "  name VARCHAR(100) NOT NULL,\n" +
                "  email VARCHAR(100) NOT NULL,\n" +
                "  password VARCHAR(100) NOT NULL\n" +
                ");");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS index_user_name on users (name);");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS index_user_email ON users (email);");
    }

}
