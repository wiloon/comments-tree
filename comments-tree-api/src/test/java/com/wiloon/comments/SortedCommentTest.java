package com.wiloon.comments;

import com.wiloon.comments.comment.CommentService;
import com.wiloon.comments.comment.CommentsTreeNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SortedCommentTest {

    @Autowired
    private CommentService commentService;

    @Test
    public void testSortedComments() {
        CommentsTreeNode root = commentService.getSortedComments();
        System.out.println(root);
    }

    @Test
    public void testNewComment() throws Exception {
        int id = commentService.newComment("content0内容0", "userId0",0);
        System.out.println("id:" + id);
        Assert.assertTrue(id > 0);
        int next = commentService.newComment("content1内容1", "userId1",0);
        System.out.println("next:" + next);
        Assert.assertTrue(next > id);
    }
}
