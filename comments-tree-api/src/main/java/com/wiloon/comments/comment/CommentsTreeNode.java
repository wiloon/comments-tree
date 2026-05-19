package com.wiloon.comments.comment;

import com.alibaba.fastjson.annotation.JSONField;

import java.sql.Timestamp;
import java.util.TreeSet;

/**
 * Comment tree node
 * @author wiloon
 */
public class CommentsTreeNode implements Comparable<CommentsTreeNode> {
    /**
     * Comment
     */
    private Comment comment;
    /**
     * Replies to this comment
     */
    private TreeSet<CommentsTreeNode> reply;

    private CommentsTreeNode() {

    }

    /**
     * Create a new comment node
     *
     * @param comment the comment
     * @return tree node
     */
    public static CommentsTreeNode newNode(Comment comment) {
        CommentsTreeNode node = new CommentsTreeNode();
        node.setComment(comment);
        node.setReply(new TreeSet<>());
        return node;
    }

    /**
     * Create a virtual node, e.g. a virtual root node.
     *
     * @param commentID comment id
     * @return tree node
     */
    public static CommentsTreeNode newNode(int commentID) {
        CommentsTreeNode node = new CommentsTreeNode();
        Comment tmp = new Comment();
        tmp.setId(commentID);
        tmp.setUserName("");
        tmp.setContent("");
        tmp.setParentId(-1);
        node.setComment(tmp);
        node.setReply(new TreeSet<>());
        return node;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    /**
     * Add a direct reply to this comment node
     *
     * @param node reply node
     */
    public void addReply(CommentsTreeNode node) {
        reply.add(node);
    }

    /**
     * Compare nodes at the same level by update time in descending order
     *
     * @param o the other node
     * @return 0 if equal, -1 if newer, 1 if older
     */
    @Override
    public int compareTo(CommentsTreeNode o) {
        if (this.getComment().getId().equals(o.getComment().getId())) {
            return 0;
        } else {
            return this.getComment().getUpdateTime().after(o.getComment().getUpdateTime()) ? -1 : 1;
        }

    }

    /**
     * Get replies to this comment
     *
     * @return reply set
     */
    public TreeSet<CommentsTreeNode> getReply() {
        return reply;
    }

    public int getParentId() {
        return this.comment.getParentId();
    }

    public int getId() {
        return this.comment.getId();
    }

    public String getContent() {
        return this.comment.getContent();
    }

    public String getUserName() {
        return this.comment.getUserName();
    }

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Timestamp getUpdateTime() {
        return this.comment.getUpdateTime();
    }

    public int getReplySize() {
        return reply == null ? 0 : reply.size();
    }

    public void setReply(TreeSet<CommentsTreeNode> reply) {
        this.reply = reply;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
