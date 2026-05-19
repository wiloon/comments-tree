package com.wiloon.comments;

import com.wiloon.comments.comment.CommentService;
import com.wiloon.comments.comment.CommentsTreeNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TreeSet;

@SpringBootTest
public class CommentDaoTest {

    @Autowired
    CommentService commentService;

    @Test
    public void test() {
        int commentId = commentService.newComment("content0", "user0", 0);
        Assertions.assertTrue(commentId > 0);
        TreeSet<CommentsTreeNode> comments = commentService.getSortedComments();
        Assertions.assertNotNull(comments);
        Assertions.assertEquals(1, comments.size());
        commentService.deleteComment(commentId);
    }
}
