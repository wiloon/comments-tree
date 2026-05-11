package com.wiloon.comments.comment;

import com.wiloon.comments.common.CommonResult;
import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.TreeSet;

/**
 * 留言/评论 controller
 *
 * @author wiloon
 */
@RestController
@Validated
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final UserService userService;
    private final CommentService commentService;

    public CommentController(UserService userService, CommentService commentService) {
        this.userService = userService;
        this.commentService = commentService;
    }

    /**
     * 查询留言列表
     *
     * @return comments list
     */
    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    public CommonResult<TreeSet<CommentsTreeNode>> comments() {
        logger.info("get comment list");
        TreeSet<CommentsTreeNode> commentsTreeNode = commentService.getSortedComments();
        return CommonResult.success(commentsTreeNode);
    }

    /**
     * 新建留言
     *
     * @param comment: content: 留言内容, parentId: 父节点ID
     * @return 结果数据: 成功,失败
     */
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public CommonResult<String> newComment(@RequestBody Comment comment) {
        String content = comment.getContent();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByNameOrEmail(authentication.getName());
        if (user == null) {
            return CommonResult.failed("Authenticated user not found");
        }
        String userId = user.getId();
        logger.info("new comment params: {}, user id: {}", comment, userId);
        try {
            int id = commentService.newComment(content, userId, comment.getParentId());
            logger.info("new comment created, id: {}", id);
            return CommonResult.success("保存成功");
        } catch (Exception e) {
            logger.error("failed to save comment", e);
            return CommonResult.failed("保存失败");
        }
    }
}
