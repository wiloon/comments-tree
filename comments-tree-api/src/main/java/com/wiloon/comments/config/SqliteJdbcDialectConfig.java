package com.wiloon.comments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.MySqlDialect;

import java.sql.Timestamp;
import java.util.List;

/**
 * Spring Data JDBC does not ship a SQLite dialect. MySQL dialect is close enough for
 * SQLite AUTOINCREMENT and generated-key retrieval used by this application.
 */
@Configuration
public class SqliteJdbcDialectConfig {

    @Bean
    public Dialect jdbcDialect() {
        return MySqlDialect.INSTANCE;
    }

    /**
     * SQLite JDBC returns epoch milliseconds as Long for TIMESTAMP columns.
     */
    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(new Converter<Long, Timestamp>() {
            @Override
            public Timestamp convert(Long source) {
                return new Timestamp(source);
            }
        }));
    }
}
