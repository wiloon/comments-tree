package com.wiloon.comments.comment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Comment service
 *
 * @author wiloon
 */
@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Returns comments sorted in tree structure
     *
     * @return root node of the comments tree
     */
    public TreeSet<CommentsTreeNode> getSortedComments() {
        List<Comment> comments = commentRepository.findAllCommentsWithTreePath();
        log.debug("comments size: {}", comments.size());
        Map<Integer, CommentsTreeNode> tmpMap = new HashMap<>(comments.size() + 1);
        for (Comment comment : comments) {
            log.debug("parent id: {}, comment id: {}", comment.getParentId(), comment.getId());
            CommentsTreeNode currentNode = CommentsTreeNode.newNode(comment);
            int parentId = currentNode.getParentId();
            int id = currentNode.getId();
            if (!tmpMap.containsKey(parentId)) {
                tmpMap.put(currentNode.getParentId(), CommentsTreeNode.newNode(currentNode.getParentId()));
            }
            CommentsTreeNode parentNode = tmpMap.get(parentId);
            parentNode.addReply(currentNode);
            log.debug("comment id: {}, reply size: {}", parentNode.getId(), parentNode.getReplySize());
            if (!tmpMap.containsKey(id) || tmpMap.get(id).getParentId() == -1) {
                tmpMap.put(id, currentNode);
            }
        }
        if (tmpMap.get(Comment.ROOT_NODE_ID) == null) {
            return null;
        }
        return tmpMap.get(Comment.ROOT_NODE_ID).getReply();
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
        Timestamp now = new Timestamp(System.currentTimeMillis());
        CommentEntity entity = new CommentEntity();
        entity.setContent(content);
        entity.setUserId(userId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        CommentEntity saved = commentRepository.save(entity);
        if (saved.getId() == null) {
            throw new RuntimeException("failed to save comment");
        }
        commentRepository.insertTreePath(parentId, saved.getId());
        return saved.getId();
    }

    public void deleteComment(int id) {
        commentRepository.deleteById(id);
    }
}
