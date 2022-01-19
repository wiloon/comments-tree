package com.wiloon.p219;

import org.junit.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;

public class CommentTest {

    @Test
    public void testPrimaryKey() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:jdbc/schema.sql")
                .addScript("classpath:jdbc/test-data.sql")
                .build();


        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        final String sql = " INSERT INTO comment (content,user_id,create_time,update_time) VALUES ('content0','0',now(),now())";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("offsetNum", 0);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int autoIncId = 0;

        jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        autoIncId = keyHolder.getKey().intValue();

        System.out.println(autoIncId);

        jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        autoIncId = keyHolder.getKey().intValue();

        System.out.println(autoIncId);

    }

}
