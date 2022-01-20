package com.wiloon.comments.comment;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
