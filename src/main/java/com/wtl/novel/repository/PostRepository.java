package com.wtl.novel.repository;

import com.wtl.novel.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 根据 postType 查询并分页排序
    @Query("SELECT p FROM Post p WHERE p.postType = :postType")
    Page<Post> findByPostType(@Param("postType") Integer postType, Pageable pageable);

    // 根据 postType 查询并分页排序
    @Query("SELECT p FROM Post p WHERE p.collections = :novelId")
    Page<Post> findByPostTypeAndNovelId(@Param("novelId") Long novelId, Pageable pageable);

    List<Post> findByPostType(Integer postType);
    // 根据 ID 将 commentNum 加 1
    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.commentNum = p.commentNum + 1 WHERE p.id = :id")
    void incrementCommentNumById(@Param("id") Long id);
}