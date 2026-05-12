package com.wiloon.comments.comment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Comment service
 *
 * @author wiloon
 */
@Slf4j
@Service
public class CommentService {
    private final CommentsDao commentsDao;

    public CommentService(CommentsDao commentsDao) {
        this.commentsDao = commentsDao;
    }

    /**
     * Returns comments sorted in tree structure
     *
     * @return root node of the comments tree
     */
    public TreeSet<CommentsTreeNode> getSortedComments() {
        List<Comment> comments = commentsDao.getAllComments();
        log.debug("comments size: {}", comments.size());
        // Temporary map for collecting comment dependencies
        // key: comment id, value: comment tree node
        Map<Integer, CommentsTreeNode> tmpMap = new HashMap<>(comments.size() + 1);
        for (Comment comment : comments) {
            // Fill the comment tree in loop
            log.debug("parent id: {}, comment id: {}", comment.getParentId(), comment.getId());
            // Wrap comment into tree node
            CommentsTreeNode currentNode = CommentsTreeNode.newNode(comment);
            int parentId = currentNode.getParentId();
            int id = currentNode.getId();
            if (!tmpMap.containsKey(parentId)) {
                // If parent node not in map, create a virtual tree node; handles root node creation or out-of-order DB results
                tmpMap.put(currentNode.getParentId(), CommentsTreeNode.newNode(currentNode.getParentId()));
            }
            // Retrieve parent node from map
            CommentsTreeNode parentNode = tmpMap.get(parentId);
            // Add current node to parent's reply collection
            parentNode.addReply(currentNode);
            log.debug("comment id: {}, reply size: {}", parentNode.getId(), parentNode.getReplySize());
            if (!tmpMap.containsKey(id) || tmpMap.get(id).getParentId() == -1) {
                // Add self reference to map for collecting replies
                tmpMap.put(id, currentNode);
            }
        }
        // Return the comment tree
        if (tmpMap.get(Comment.ROOT_NODE_ID) == null) {
            return null;
        } else {
            return tmpMap.get(Comment.ROOT_NODE_ID).getReply();
        }

    }

    /**
     * Create a new comment
     *
     * @param content  comment text
     * @param userId   user ID
     * @param parentId parent node ID of this comment
     * @return ID of the new comment (auto-increment primary key)
     */

    @Transactional(rollbackFor = Exception.class)
    public int newComment(String content, String userId, int parentId) {
        int commentId = commentsDao.insertComment(content, userId);
        commentsDao.insertCommentTreePath(parentId, commentId);
        return commentId;

    }
}
