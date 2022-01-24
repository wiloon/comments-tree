package com.wiloon.comments.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

/**
 * 留言/评论 Dao
 */
@Repository
public class CommentsDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询所有留言和评论，关联 comments 表和 comments_tree_path 表，原始数据。
     *
     * @return Comment 列表，按parent id, id 倒序
     */
    public List<Comment> getAllComments() {
        String sql = "SELECT ctp.parent_id,c.id,c.content,c.create_time," +
                "c.update_time,u.name as user_name, c.update_time " +
                "from comments c JOIN comments_tree_path ctp ON c.id=ctp.child_id " +
                "join users u on c.user_id=u.id " +
                "order by ctp.parent_id,ctp.child_id desc";
        return jdbcTemplate.query(sql, new CommentRowMapper());
    }

    /**
     * 新建留言/评论
     *
     * @param content 留言内容
     * @param userId  用户id
     * @return 留言id
     */
    public int insertComment(String content, String userId) {
        String sql = "INSERT INTO comments (content, user_id,create_time,update_time) VALUES (?,?,?,?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            // 指定主键
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, content);
            preparedStatement.setString(2, userId);
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            return preparedStatement;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new RuntimeException("failed to save comment");
        }
        return keyHolder.getKey().intValue();
    }

    /**
     * 留言/评论关系表
     *
     * @param parentId  父节点 id
     * @param commentId 留言 id
     */
    public void insertCommentTreePath(int parentId, int commentId) {
        String treePathSql = "INSERT INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (?, ?);";
        jdbcTemplate.update(treePathSql, parentId, commentId);
    }

}
