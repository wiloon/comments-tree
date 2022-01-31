package com.wiloon.comments.comment;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.TreeSet;

/**
 * 留言树节点
 * @author wiloon
 */
public class CommentsTreeNode implements Comparable<CommentsTreeNode> {
    /**
     * 留言
     */
    private Comment comment;
    /**
     * 此条留言的评论集合
     */
    private TreeSet<CommentsTreeNode> reply;

    private CommentsTreeNode() {

    }

    /**
     * 创建一个新的留言节点
     *
     * @param comment 留言
     * @return 树节点
     */
    public static CommentsTreeNode newNode(Comment comment) {
        CommentsTreeNode node = new CommentsTreeNode();
        node.setComment(comment);
        node.setReply(new TreeSet<>());
        return node;
    }

    /**
     * 创建一个虚拟的节点，如: 虚拟的根节点。
     *
     * @param commentID 留言id
     * @return 树节点
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
     * 向留言树填充留言的直接评论
     *
     * @param node 评论节点
     */
    public void addReply(CommentsTreeNode node) {
        reply.add(node);
    }

    /**
     * 比较同一层的留言顺序按更新日期倒序
     *
     * @param o 留言
     * @return 相等: 0, 时间更新: -1, 时间更旧: 1
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
     * 获取留言的评论
     *
     * @return 评论集合
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
    public Date getUpdateTime() {
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
