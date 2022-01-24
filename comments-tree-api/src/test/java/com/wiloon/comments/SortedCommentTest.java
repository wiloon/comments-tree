package com.wiloon.comments;

import com.wiloon.comments.comment.Comment;
import com.wiloon.comments.comment.CommentService;
import com.wiloon.comments.comment.CommentsDao;
import com.wiloon.comments.comment.CommentsTreeNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SortedCommentTest {
    @MockBean
    private CommentsDao commentsDao;

    @Autowired
    CommentService commentService;

    @Test
    public void testSortedComments() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<Comment> list = new ArrayList<>();
            list.add(new Comment(0, 1, sdf.parse("2022-01-23 00:00:00")));
            list.add(new Comment(0, 2, sdf.parse("2022-01-23 00:01:00")));
            list.add(new Comment(0, 3, sdf.parse("2022-01-23 00:02:00")));

            list.add(new Comment(1, 4, sdf.parse("2022-01-23 00:03:00")));

            Mockito.when(commentsDao.getAllComments()).thenReturn(list);
            CommentsTreeNode root = commentService.getSortedComments();
            Assert.assertNotNull(root);
            Assert.assertNotNull(root.getComment());
            Assert.assertEquals(3, root.getReply().size());
            Assert.assertTrue(3 == root.getReply().first().getComment().getId());
            Assert.assertTrue(1 == root.getReply().last().getId());
            Assert.assertTrue(4 == root.getReply().last().getReply().first().getId());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNewComment() throws Exception {
        int id = commentService.newComment("content0内容0", "userId0", 0);
        System.out.println("id:" + id);
        Assert.assertTrue(id > 0);
        int next = commentService.newComment("content1内容1", "userId1", 0);
        System.out.println("next:" + next);
        Assert.assertTrue(next > id);
    }
}
