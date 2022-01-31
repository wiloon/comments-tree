package com.wiloon.comments;

import com.wiloon.comments.comment.CommentService;
import com.wiloon.comments.comment.CommentsDao;
import com.wiloon.comments.comment.CommentsTreeNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.TreeSet;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommentDaoTest {
    @Autowired
    CommentsDao commentsDao;
    @Autowired
    CommentService commentService;


    @Test
    public void test() {
        int commentId = commentService.newComment("content0", "user0", 0);
        Assert.assertTrue(commentId > 0);
        TreeSet<CommentsTreeNode> comments = commentService.getSortedComments();
        Assert.assertNotNull(comments);
        Assert.assertEquals(1, comments.size());
        commentsDao.deleteComment(commentId);
    }
}
