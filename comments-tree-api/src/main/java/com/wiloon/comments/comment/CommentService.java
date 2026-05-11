package com.wiloon.comments.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Comment service
 *
 * @author wiloon
 */
@Service
public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentsDao commentsDao;

    public CommentService(CommentsDao commentsDao) {
        this.commentsDao = commentsDao;
    }

    /**
     * 排序后的留言和评论
     *
     * @return comments 树形结构的根节点
     */
    public TreeSet<CommentsTreeNode> getSortedComments() {
        List<Comment> comments = commentsDao.getAllComments();
        logger.debug("comments size: {}", comments.size());
        // 临时 map 用于收集留言依赖关系
        // key: comment id, value: comment tree node
        Map<Integer, CommentsTreeNode> tmpMap = new HashMap<>(comments.size() + 1);
        for (Comment comment : comments) {
            // 循环填充留言树
            logger.debug("parent id: {}, comment id: {}", comment.getParentId(), comment.getId());
            // 留言包装成 tree node
            CommentsTreeNode currentNode = CommentsTreeNode.newNode(comment);
            int parentId = currentNode.getParentId();
            int id = currentNode.getId();
            if (!tmpMap.containsKey(parentId)) {
                // 如果父节点不在map里，创建一个虚拟的 tree node 加入 map, 创建根节点或者数据库返回无序列表时，预先创建父节点
                tmpMap.put(currentNode.getParentId(), CommentsTreeNode.newNode(currentNode.getParentId()));
            }
            // 从map里取出父节点
            CommentsTreeNode parentNode = tmpMap.get(parentId);
            // 把当前节点加到父节点的评论集合里
            parentNode.addReply(currentNode);
            logger.debug("comment id: {}, reply size: {}", parentNode.getId(), parentNode.getReplySize());
            if (!tmpMap.containsKey(id) || tmpMap.get(id).getParentId() == -1) {
                //把自己的引用加到map里，方便收集评论
                tmpMap.put(id, currentNode);
            }
        }
        // 返回留言树
        if (tmpMap.get(Comment.ROOT_NODE_ID) == null) {
            return null;
        } else {
            return tmpMap.get(Comment.ROOT_NODE_ID).getReply();
        }

    }

    /**
     * 新建留言/评论
     *
     * @param content  留言内容
     * @param userId   用户 ID
     * @param parentId 本留言的父节点 ID
     * @return 本留言/评论 的 ID, comments 表自增主键新值
     */

    @Transactional(rollbackFor = Exception.class)
    public int newComment(String content, String userId, int parentId) {
        int commentId = commentsDao.insertComment(content, userId);
        commentsDao.insertCommentTreePath(parentId, commentId);
        return commentId;

    }
}
