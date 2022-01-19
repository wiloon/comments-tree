package com.wiloon.p219.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save() {
        jdbcTemplate.execute("INSERT INTO beers VALUES ('Stella')");
    }
}
