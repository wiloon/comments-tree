package com.wiloon.comments;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.comment.Comment;
import com.wiloon.comments.comment.CommentRowMapper;
import com.wiloon.comments.comment.CommentService;
import com.wiloon.comments.comment.CommentsTreeNode;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CommentTest {
    @Test
    public void testSortedComments() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:jdbc/schema.sql")
                .addScript("classpath:jdbc/test-data.sql")
                .build();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        CommentService commentService = new CommentService();
        commentService.setJdbcTemplate(jdbcTemplate);
        CommentsTreeNode root = commentService.getSortedComments();
        System.out.println(JSON.toJSONString(root));

    }

    @Test
    public void testCommentsList() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:jdbc/schema.sql")
                .addScript("classpath:jdbc/test-data.sql")
                .build();


        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> commentsList = jdbcTemplate.queryForList("SELECT c.id,c.content,c.update_time,ctp.parent_id from comments c JOIN comments_tree_path ctp ON c.id=ctp.child_id");
        System.out.println(commentsList.size());
        String sql = "SELECT ctp.parent_id,c.id,c.content,c.user_id,c.create_time,c.update_time from comments c JOIN comments_tree_path ctp ON c.id=ctp.child_id ORDER BY ctp.parent_id,ctp.child_id";
        List<Comment> comments = jdbcTemplate.query(sql, new CommentRowMapper());
        System.out.println(comments.size());
    }

    @Test
    public void testPrimaryKey() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:jdbc/schema.sql")
                .addScript("classpath:jdbc/test-data.sql")
                .build();


        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        final String sql = " INSERT INTO comments (content,user_id,create_time,update_time) VALUES ('content0','0',now(),now())";
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
