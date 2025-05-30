package com.wtl.novel.Service;

import com.wtl.novel.CDO.CommentTree;
import com.wtl.novel.entity.PostComment;
import com.wtl.novel.repository.PostCommentRepository;
import com.wtl.novel.repository.PostRepository;
import com.wtl.novel.util.MaskStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostCommentService {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;


    // 获取评论树（顶级评论及其子评论）
    public List<CommentTree> getCommentTreeByPostId(Long postId) {
        // 获取所有评论
        List<PostComment> allComments = postCommentRepository.findByPostId(postId);

        // 对顶级评论的用户名进行脱敏
//        allComments.forEach(commentTree -> {
//            commentTree.setUsername(MaskStringUtil.maskMiddle(commentTree.getUsername()));
//        });

        // 将评论转换为 CommentTree 类型
        List<CommentTree> commentTrees = allComments.stream()
                .map(CommentTree::new)
                .toList();

        // 构建评论树
        Map<Long, CommentTree> commentMap = new HashMap<>();
        for (CommentTree comment : commentTrees) {
            commentMap.put(comment.getId(), comment);
        }

        // 为每个评论添加子评论
        List<CommentTree> rootComments = new ArrayList<>();
        for (CommentTree comment : commentTrees) {
            if (comment.getParentId() == null) {
                // 如果是顶级评论，直接加入根列表
                rootComments.add(comment);
            } else {
                // 如果是子评论，找到父评论并添加到父评论的 children 中
                CommentTree parentComment = commentMap.get(comment.getParentId());
                if (parentComment != null) {
                    parentComment.addChild(comment);
                }
            }
        }

        return rootComments;
    }


    public List<PostComment> getAllCommentsByPostId(Long postId) {
        List<PostComment> byPostId = postCommentRepository.findByPostId(postId);
        byPostId.forEach(postComment -> {
            postComment.setUsername(MaskStringUtil.maskMiddle(postComment.getUsername()));
        });
        return byPostId;
    }

    public PostComment createComment(PostComment comment) {
        postRepository.incrementCommentNumById(comment.getPostId());
        return postCommentRepository.save(comment);
    }

    public PostComment likeComment(Long id) {
        PostComment comment = postCommentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setLikes(comment.getLikes() + 1);
            return postCommentRepository.save(comment);
        }
        return null;
    }

    public List<PostComment> getRepliesByCommentId(Long parentId) {
        return postCommentRepository.findByParentId(parentId);
    }
}