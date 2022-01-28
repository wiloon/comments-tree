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
            list.add(new Comment(1, 5, sdf.parse("2022-01-23 00:04:00")));
            list.add(new Comment(4, 6, sdf.parse("2022-01-23 00:06:00")));

            list.add(new Comment(3, 7, sdf.parse("2022-01-23 00:07:00")));

            Mockito.when(commentsDao.getAllComments()).thenReturn(list);
            CommentsTreeNode root = commentService.getSortedComments();
            Assert.assertNotNull(root);
            Assert.assertNotNull(root.getComment());
            Assert.assertEquals(3, root.getReply().size());
            Assert.assertEquals(3, (int) root.getReply().first().getComment().getId());
            Assert.assertEquals(1, root.getReply().last().getId());
            Assert.assertEquals(5, root //0
                    .getReply().last() // 1
                    .getReply().first().getId());
            Assert.assertEquals(4, root //0
                    .getReply().last() //1
                    .getReply().last().getId()); // 4

            Assert.assertEquals(6, root //0
                    .getReply().last() // 1
                    .getReply().last() //4
                    .getReply().first().getId());

            Assert.assertEquals(7, root // 0
                    .getReply().first() // 3
                    .getReply().last().getId());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
