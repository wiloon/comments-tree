package com.wiloon.comments.comment;

import java.util.Date;

public class CommentRaw {
    // 本条留言的id
    protected Integer id;
    // 留言内容
    protected String content;
    // 创建时间
    protected Date createTime;
    // 修改时间
    protected Date updateTime;

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
}
