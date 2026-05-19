package com.wiloon.comments;

import com.wiloon.comments.comment.Comment;
import com.wiloon.comments.comment.CommentRepository;
import com.wiloon.comments.comment.CommentService;
import com.wiloon.comments.comment.CommentsTreeNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@ExtendWith(MockitoExtension.class)
public class SortedCommentTest {

    @Mock
    private CommentRepository commentRepository;

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

        Mockito.when(commentRepository.findAllCommentsWithTreePath()).thenReturn(list);
        TreeSet<CommentsTreeNode> topComments = commentService.getSortedComments();
        Assertions.assertNotNull(topComments);
        Assertions.assertEquals(3, topComments.size());
        Assertions.assertEquals(3, (int) topComments.first().getComment().getId());
        Assertions.assertEquals(1, topComments.last().getId());
        Assertions.assertEquals(5, topComments.last()
                .getReply().first().getId());
        Assertions.assertEquals(4, topComments.last()
                .getReply().last().getId());
        Assertions.assertEquals(6, topComments.last()
                .getReply().last()
                .getReply().first().getId());
        Assertions.assertEquals(7, topComments.first()
                .getReply().last().getId());
    }
}
