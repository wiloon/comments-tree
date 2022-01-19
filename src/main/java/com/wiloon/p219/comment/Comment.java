package com.wiloon.p219.comment;

import java.time.LocalDateTime;

public class Comment {
    // 父节点id
    private String parentId;
    // 本条留言的id
    private String id;
    // 留言内容
    private String content;
    // 用户信息
    private String user;
    // 创建时间
    private LocalDateTime createTime;
    // 修改时间
    private LocalDateTime updateTime;
}
