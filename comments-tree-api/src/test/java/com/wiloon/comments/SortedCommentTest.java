package com.wiloon.comments;

import com.wiloon.comments.comment.Comment;
import com.wiloon.comments.comment.CommentService;
import com.wiloon.comments.comment.CommentsDao;
import com.wiloon.comments.comment.CommentsTreeNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@RunWith(MockitoJUnitRunner.class)
public class SortedCommentTest {

    @Mock
    private CommentsDao commentsDao;

    @InjectMocks
    private CommentService commentService;

    @Test
    public void testSortedComments() throws ParseException {
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
        TreeSet<CommentsTreeNode> topComments = commentService.getSortedComments();
        Assert.assertNotNull(topComments);
        Assert.assertEquals(3, topComments.size());
        Assert.assertEquals(3, (int) topComments.first().getComment().getId());
        Assert.assertEquals(1, topComments.last().getId());
        Assert.assertEquals(5, topComments.last()       // 1
                .getReply().first().getId());
        Assert.assertEquals(4, topComments.last()       // 1
                .getReply().last().getId());             // 4
        Assert.assertEquals(6, topComments.last()       // 1
                .getReply().last()                       // 4
                .getReply().first().getId());
        Assert.assertEquals(7, topComments.first()      // 3
                .getReply().last().getId());
    }
}
