package com.wiloon.comments.comment;

import com.wiloon.comments.common.CommonResult;
import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.TreeSet;

/**
 * Comment controller
 *
 * @author wiloon
 */
@Slf4j
@RestController
@Validated
public class CommentController {
    private final UserService userService;
    private final CommentService commentService;

    public CommentController(UserService userService, CommentService commentService) {
        this.userService = userService;
        this.commentService = commentService;
    }

    /**
     * Get comment list
     *
     * @return comments list
     */
    @GetMapping("/comments")
    public CommonResult<TreeSet<CommentsTreeNode>> comments() {
        log.info("get comment list");
        TreeSet<CommentsTreeNode> commentsTreeNode = commentService.getSortedComments();
        return CommonResult.success(commentsTreeNode);
    }

    /**
     * Create a new comment
     *
     * @param comment: content: comment text, parentId: parent node ID
     * @return result: success or failure
     */
    @PostMapping("/comment")
    public CommonResult<String> newComment(@RequestBody @Valid Comment comment) {
        String content = comment.getContent();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByNameOrEmail(authentication.getName());
        if (user == null) {
            return CommonResult.failed("Authenticated user not found");
        }
        String userId = user.getId();
        log.info("new comment params: {}, user id: {}", comment, userId);
        try {
            int id = commentService.newComment(content, userId, comment.getParentId());
            log.info("new comment created, id: {}", id);
            return CommonResult.success("Saved successfully");
        } catch (Exception e) {
            log.error("failed to save comment", e);
            return CommonResult.failed("Failed to save");
        }
    }
}
