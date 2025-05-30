package com.wtl.novel.repository;

import com.wtl.novel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findUserById(Long userId);
    // 新增方法：根据 userId 获取 point
    @Query("SELECT u.point FROM User u WHERE u.id = :userId")
    Long findPointByUserId(@Param("userId") Long userId);

    // 新增方法：根据 userId 将积分加 1
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.point = u.point + 100 WHERE u.id = :userId")
    int increasePointByUserId(@Param("userId") Long userId);

    // 新增方法：如果用户的 point 大于等于指定的积分数，扣掉指定的积分数，返回 true；否则返回 false
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.point = u.point - :points WHERE u.id = :userId AND u.point >= :points")
    int decreasePointByUserId(@Param("userId") Long userId, @Param("points") Long points);

    // 新增方法：检查是否成功扣除积分
    default boolean checkAndDecreasePoints(Long userId, Long points) {
        int updatedRows = decreasePointByUserId(userId, points);
        return updatedRows > 0;
    }


    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.upload = :upload WHERE u.id = :userId")
    int updateUploadByUserId(@Param("userId") Long userId, @Param("upload") Boolean upload);
}