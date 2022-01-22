package com.wiloon.comments.comment;

import java.util.Date;
import java.util.Objects;

public class Comment {
    // 父节点id
    private Integer parentId;
    // 本条留言的id
    private Integer id;
    // 留言内容
    private String content;
    // 用户信息
    private String userName;
    // 创建时间
    private Date createTime;
    // 修改时间
    private Date updateTime;

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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
