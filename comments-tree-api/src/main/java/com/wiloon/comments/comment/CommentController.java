package com.wiloon.comments.comment;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 留言/评论 controller
 */
@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    @Autowired
    UserService userService;
    @Autowired
    CommentService commentService;

    /**
     * 查询留言列表
     *
     * @return comments list
     */
    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    @ResponseBody
    public String comments() {
        logger.info("get comment list");
        CommentsTreeNode commentsTreeNode = commentService.getSortedComments();
        String comments = JSON.toJSONString(CommonResult.success(commentsTreeNode));
        logger.debug("comment list: {}", comments);
        return comments;
    }

    /**
     * 新建留言
     * @param jsonParam: content: 留言内容, parentId: 父节点ID
     * @return 结果数据: 成功,失败
     */
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    @ResponseBody
    public String newComment(@RequestBody JSONObject jsonParam) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByNameOrEmail(authentication.getName());
        String userId = user.getId();
        logger.info("new comment params: {}, user id: {}", jsonParam.toStringPretty(), userId);
        try {
            int id = commentService.newComment(jsonParam.getStr("content"), userId, jsonParam.getInt("parentId"));
            logger.info("new comment created, id: {}", id);
            return JSON.toJSONString(CommonResult.success("msg save"));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(CommonResult.failed("failed to create comment"));
        }
    }
}
