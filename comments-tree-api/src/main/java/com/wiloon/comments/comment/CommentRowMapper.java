package com.wiloon.comments.comment;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Comment Sqlite row mapper
 */
public class CommentRowMapper implements RowMapper<Comment> {
    @Override
    public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Comment comment = new Comment();
        comment.setParentId(rs.getInt("parent_id"));
        comment.setId(rs.getInt("id"));
        comment.setContent(rs.getString("content"));
        comment.setUserName(rs.getString("user_name"));
        comment.setCreateTime(rs.getDate("create_time"));
        comment.setUpdateTime(rs.getDate("update_time"));

        return comment;
    }
}
