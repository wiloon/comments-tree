package com.wiloon.comments.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save() {
        jdbcTemplate.execute("INSERT INTO beers VALUES ('Stella')");
    }

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
}
