package com.wiloon.comments.comment;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import com.wiloon.comments.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    CommentService commentService;

    /**
     * get all comments
     *
     * @return comments list
     */
    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    @ResponseBody
    public String comments() {
        CommentsTreeNode commentsTreeNode = commentService.getSortedComments();
        String comments = JSON.toJSONString(CommonResult.success(commentsTreeNode));
        logger.debug("comments: {}", comments);
        return comments;
    }

    // 新建留言
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    @ResponseBody
    public String newComment(@RequestBody JSONObject jsonParam, HttpSession session) {
        String userId = (String) session.getAttribute(User.SESSION_USER_ID_KEY);
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
