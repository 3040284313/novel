package com.wtl.novel.repository;

import com.wtl.novel.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    // 根据postId获取所有评论（包括顶级评论和回复）
    List<PostComment> findByPostId(Long postId);

    // 根据parentId获取所有回复
    List<PostComment> findByParentId(Long parentId);
}