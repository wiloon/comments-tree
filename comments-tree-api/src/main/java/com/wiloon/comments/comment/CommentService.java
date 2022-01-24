package com.wiloon.comments.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Comment service
 */
@Service
public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    CommentsDao commentsDao;

    /**
     * 排序后的留言和评论
     *
     * @return comments 树形结构的根节点
     */
    public CommentsTreeNode getSortedComments() {
        List<Comment> comments = commentsDao.getAllComments();
        logger.debug("comments size: {}", comments.size());
        // key: comment id, value: comment tree node
        Map<Integer, CommentsTreeNode> tmpMap = new HashMap<>();
        for (Comment comment : comments) {
            logger.debug("parent id: {}, comment id: {}", comment.getParentId(), comment.getId());

            CommentsTreeNode currentNode = CommentsTreeNode.NewNode(comment);
            int parentId = currentNode.getParentId();
            int id = currentNode.getId();
            if (!tmpMap.containsKey(parentId)) {
                // 如果父节点不在map里，创建一个 虚拟的tree node 加入 map, 创建根节点或者数据库返回无序列表时，预先创建父节点
                tmpMap.put(currentNode.getParentId(), CommentsTreeNode.NewDummyNode(currentNode.getParentId()));
            }
            // 从map里取出父节点，把当前节点加到父节点的评论集合里
            CommentsTreeNode parentNode = tmpMap.get(parentId);
            parentNode.addReply(currentNode);
            logger.debug("comment id: {}, reply size: {}",
                    parentNode.getComment() == null ? 0 : parentNode.getComment().getId(),
                    parentNode.getReply() == null ? 0 : parentNode.getReply().size());
            if (!tmpMap.containsKey(id) || tmpMap.get(id).isDummy()) {
                //把自己的引用加到map里，方便收集评论
                tmpMap.put(id, currentNode);
            }
        }
        // 返回根节点
        return tmpMap.get(0);
    }

    /**
     * 新建留言/评论
     *
     * @param content  留言内容
     * @param userId   用户 ID
     * @param parentId 本留言的父节点 ID
     * @return 本留言/评论 的 ID, comments 表自增主键新值
     */
    @Transactional
    public int newComment(String content, String userId, int parentId) {
        int commentId = commentsDao.insertComment(content, userId);
        commentsDao.insertCommentTreePath(parentId, commentId);
        return commentId;

    }
}
