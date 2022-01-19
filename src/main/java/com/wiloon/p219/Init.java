package com.wiloon.p219;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Map;

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
        // user
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (\n" +
                "  id char(36) PRIMARY KEY NOT NULL,\n" +
                "  name VARCHAR(100) NOT NULL,\n" +
                "  email VARCHAR(100) NOT NULL,\n" +
                "  password VARCHAR(100) NOT NULL\n" +
                ");");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS index_user_name on users (name);");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS index_user_email ON users (email);");
        List<Map<String, Object>> users= jdbcTemplate.queryForList("SELECT * FROM users  where id=?","c31f5e0e-0e0c-4731-97dc-9c6675a0068c");
        logger.info("users: {}",users);

        // default admin user
        if (users.size() == 0)
            jdbcTemplate.execute("INSERT INTO users VALUES ('c31f5e0e-0e0c-4731-97dc-9c6675a0068c','admin','admin@admin.com','$2a$10$AR7t1b/kgGS2oiTrlrW2C.JkVAOT3ZviKj.2zvWZIm0lBnsOrTuX2')");

        // comment
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS comment(\n" +
                "  id INTEGER AUTO_INCREMENT NOT NULL,\n" +
                "  content VARCHAR(300) NOT NULL,\n" +
                "  user_id char(36)  NOT NULL,\n" +
                "  create_time timestamp NOT NULL,\n" +
                "  update_time timestamp NOT NULL,\n" +
                "  PRIMARY KEY (id)\n" +
                ");");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tree_path(\n" +
                "  parent INTEGER NOT NULL,\n" +
                "  child INTEGER NOT NULL,\n" +
                "  PRIMARY KEY (parent, child)\n" +
                ");");
    }

}
