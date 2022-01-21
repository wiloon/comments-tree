package com.wiloon.comments.comment;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
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

    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    @ResponseBody
    public String comments() {
        CommentsTreeNode commentsTreeNode = commentService.getSortedComments();
        String out = JSON.toJSONString(CommonResult.success(commentsTreeNode));
        logger.debug("comments: {}", out);
        return out;
    }

    // 新建留言
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    @ResponseBody
    public String newComment(@RequestBody JSONObject jsonParam, HttpSession session) {
        logger.info("messageSave params: {}", jsonParam.toStringPretty());
        String UserId = (String) session.getAttribute("userId");

        try {
            int id = commentService.newComment(jsonParam.getStr("content"), UserId, jsonParam.getInt("parentId"));
            logger.info("new comment created, id: {}", id);
            return JSON.toJSONString(CommonResult.success("msg save"));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(CommonResult.failed("failed to create comment"));
        }
    }
}
