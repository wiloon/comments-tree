package com.wiloon.comments;

import com.wiloon.comments.comment.CommentService;
import com.wiloon.comments.comment.CommentsTreeNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
}
