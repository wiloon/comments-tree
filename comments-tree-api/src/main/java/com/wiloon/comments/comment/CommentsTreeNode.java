package com.wiloon.comments.comment;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.TreeSet;

/**
 * 留言树节点
 */
public class CommentsTreeNode implements Comparable<CommentsTreeNode> {
    // 留言
    private Comment comment;
    // 此条留言的评论集合
    private TreeSet<CommentsTreeNode> reply;
    // 虚拟的节点标记
    private transient boolean dummy;

    /**
     * 创建一个新的留言节点
     *
     * @param comment 留言
     * @return 树节点
     */
    public static CommentsTreeNode NewNode(Comment comment) {
        CommentsTreeNode node = new CommentsTreeNode();
        node.setComment(comment);
        return node;
    }

    /**
     * 创建一个虚拟的节点，如: 虚拟的根节点。
     * @param CommentId 留言id
     * @return 树节点
     */
    public static CommentsTreeNode NewDummyNode(int CommentId) {
        CommentsTreeNode node = new CommentsTreeNode();
        Comment tmp = new Comment();
        tmp.setId(CommentId);
        tmp.setUserName("");
        tmp.setContent("");
        tmp.setParentId(-1);
        node.setComment(tmp);
        node.setDummy(true);
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
        if (reply == null) {
            this.reply = new TreeSet<>();
        }
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

    public boolean isDummy() {
        return dummy;
    }

    public void setDummy(boolean dummy) {
        this.dummy = dummy;
    }
}
