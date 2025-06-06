package com.wtl.novel.repository;

import com.wtl.novel.entity.UserNovelRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserNovelRelationRepository extends JpaRepository<UserNovelRelation, Long> {

    // 根据用户ID查询所有记录
    List<UserNovelRelation> findByUserId(Long userId);

    // 根据小说ID查询所有记录
    List<UserNovelRelation> findByNovelId(Long novelId);

    // 根据用户ID和小说ID查询记录
    Optional<UserNovelRelation> findByUserIdAndNovelId(Long userId, Long novelId);

    // 检查是否存在用户ID和小说ID的记录
    boolean existsByUserIdAndNovelId(Long userId, Long novelId);

    // 根据用户ID和小说ID删除记录
    void deleteByUserIdAndNovelId(Long userId, Long novelId);

    List<UserNovelRelation> findByUserIdAndPlatform(Long userId, String platform);

    List<UserNovelRelation> findByUserIdAndPlatformAndNovelId(Long userId, String platform, Long novelId);
}