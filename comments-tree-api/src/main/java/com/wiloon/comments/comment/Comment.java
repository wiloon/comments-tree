package com.wiloon.comments.comment;

import java.util.Date;
import java.util.Objects;

public class Comment extends CommentRaw {
    // 父节点id
    private Integer parentId;

    // 用户信息
    private String userName;

    public Comment() {

    }

    public Comment(int parentId, int id, Date updateTime) {
        this.parentId = parentId;
        this.id = id;
        this.updateTime = updateTime;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id.equals(comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return String.format("parent id: %s, id: %s, content: %s", this.parentId, this.id, this.content);
    }
}
