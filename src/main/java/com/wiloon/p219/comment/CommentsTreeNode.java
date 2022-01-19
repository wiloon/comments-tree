package com.wiloon.p219.comment;

import java.util.List;

public class CommentsTreeNode {
    // 留言
    private Comment comment;
    // 此条留言的评论
    private List<CommentsTreeNode> replyList;
}
