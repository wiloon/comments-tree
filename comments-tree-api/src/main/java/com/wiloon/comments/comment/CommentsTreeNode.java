package com.wiloon.comments.comment;

import java.util.TreeSet;

public class CommentsTreeNode implements Comparable<CommentsTreeNode> {
    // 留言
    private Comment comment;
    // 此条留言的评论
    private TreeSet<CommentsTreeNode> reply;

    public static CommentsTreeNode createNode(Comment comment) {
        CommentsTreeNode node = new CommentsTreeNode();
        node.setComment(comment);
        return node;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public void addNode(Comment comment) {

    }

    public void addReply(CommentsTreeNode node) {
        if (reply == null) {
            this.reply = new TreeSet<>();
        }
        reply.add(node);
    }

    @Override
    public int compareTo(CommentsTreeNode o) {
        if (this.getComment().getId().equals(o.getComment().getId())) {
            return 0;
        } else {
            return this.getComment().getUpdateTime().after(o.getComment().getUpdateTime()) ? 1 : -1;
        }

    }

    public TreeSet<CommentsTreeNode> getReply() {
        return reply;
    }

    public int getParentId() {
        return this.comment==null?-1:this.comment.getParentId();
    }

    public int getId() {
        return this.comment==null?0:this.comment.getId();
    }
    public String getContent(){
        return this.comment==null?"":this.comment.getContent();
    }
}
