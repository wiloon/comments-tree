package com.wiloon.comments.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Service
public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Comment> findAllComments() {
        String sql = "SELECT ctp.parent_id,c.id,c.content,c.user_id,c.create_time,c.update_time from comments c JOIN comments_tree_path ctp ON c.id=ctp.child_id order by ctp.parent_id,ctp.child_id";
        return jdbcTemplate.query(sql, new CommentRowMapper());
    }

    public CommentsTreeNode getSortedComments() {
        List<Comment> comments = findAllComments();
        logger.debug("comments size: {}", comments.size());
        // key: comment id, value: comment tree node
        Map<Integer, CommentsTreeNode> tmpMap = new HashMap<>();
        for (Comment comment : comments) {
            logger.debug("parent id: {}, comment id: {}, content: {}", comment.getParentId(), comment.getId(), comment.getContent());

            CommentsTreeNode currentNode = CommentsTreeNode.createNode(comment);
            int parentId = currentNode.getParentId();
            int id = currentNode.getId();
            if (!tmpMap.containsKey(parentId)) {
                tmpMap.put(currentNode.getParentId(), CommentsTreeNode.createNode(null));
            }
            CommentsTreeNode parentNode = tmpMap.get(parentId);
            parentNode.addReply(currentNode);
            logger.debug("comment id: {}, reply size: {}",
                    parentNode.getComment() == null ? 0 : parentNode.getComment().getId(),
                    parentNode.getReply() == null ? 0 : parentNode.getReply().size());
            if (!tmpMap.containsKey(id) || tmpMap.get(id) == null) {
                tmpMap.put(id, currentNode);
            }
        }
        return tmpMap.get(0);
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int newComment(String content, String UserId, int parentId) throws Exception {
        String sql = "INSERT INTO comments (content, user_id) VALUES (?,?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            // 指定主键
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, content);
            preparedStatement.setString(2, UserId);
            return preparedStatement;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new Exception("failed to save comment");
        }
        int commentId = keyHolder.getKey().intValue();
        String treePathSql = "insert INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (?, ?);";
        jdbcTemplate.update(treePathSql, parentId, commentId);
        return commentId;

    }
}
