package com.wiloon.comments.comment;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

/**
 * 留言/评论，关联查询用
 *
 * @author wiloon
 */
public class Comment extends CommentRaw {
    public static final int ROOT_NODE_ID = 0;
    /**
     * 父节点id
     */
    @NotNull
    private Integer parentId;

    /**
     * 用户信息
     */
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comment comment = (Comment) o;
        return id.equals(comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("parent id: %s, id: %s, content: %s", this.parentId, this.id, this.content);
    }
}
