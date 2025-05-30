package com.wtl.novel.repository;

import com.wtl.novel.entity.InvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    InvitationCode findByCode(String code);
    // 新增方法：根据 userId 和 used 状态查询邀请码
    Optional<InvitationCode> findByUserIdAndUsed(Long userId, boolean used);
}