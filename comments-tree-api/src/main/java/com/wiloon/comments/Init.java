package com.wiloon.comments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * init sqlite db
 */
@Component
public class Init implements ServletContextAware {
    private static final Logger logger = LoggerFactory.getLogger(Init.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void setServletContext(ServletContext servletContext) {
        logger.info("servlet context, init sqlite tables");

        jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                con.setAutoCommit(false);
                ClassPathResource classPathResource = new ClassPathResource("jdbc/schema.sql");
                EncodedResource encodedResource = new EncodedResource(classPathResource, "utf-8");
                ScriptUtils.executeSqlScript(con, encodedResource);
                con.commit();
                logger.info("sql executed: {}", classPathResource.getPath());
                return null;
            }
        });
    }
}
