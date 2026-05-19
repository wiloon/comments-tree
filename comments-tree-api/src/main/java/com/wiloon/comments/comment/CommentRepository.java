package com.wiloon.comments.comment;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends CrudRepository<CommentEntity, Integer> {

    @Modifying
    @Query("INSERT INTO comments_tree_path (parent_id, child_id) VALUES (:parentId, :childId)")
    void insertTreePath(@Param("parentId") int parentId, @Param("childId") int childId);

    @Query("""
            SELECT ctp.parent_id AS parent_id, c.id, c.content, c.create_time, c.update_time, u.name AS user_name
            FROM comments c
            INNER JOIN comments_tree_path ctp ON c.id = ctp.child_id
            LEFT JOIN users u ON c.user_id = u.id
            ORDER BY ctp.parent_id, ctp.child_id DESC
            """)
    List<Comment> findAllCommentsWithTreePath();
}
