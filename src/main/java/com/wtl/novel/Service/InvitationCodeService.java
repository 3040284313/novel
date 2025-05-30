package com.wtl.novel.Service;

import com.wtl.novel.entity.InvitationCode;
import com.wtl.novel.repository.InvitationCodeRepository;
import com.wtl.novel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvitationCodeService {
    @Autowired
    private InvitationCodeRepository invitationCodeRepository;
    @Autowired
    private UserRepository userRepository;


    public InvitationCode findByCode(String code) {
        return invitationCodeRepository.findByCode(code);
    }

    public void bindToEmail(InvitationCode invitationCode, String email) {
        invitationCode.setUsed(true);
        invitationCode.setBoundEmail(email);
        Long userId = invitationCode.getUserId();
        userRepository.increasePointByUserId(userId);
        invitationCodeRepository.save(invitationCode);
    }
    /**
     * 创建或获取邀请码
     * @param userId 用户 ID
     * @return 邀请码对象
     */
    public InvitationCode createOrGetInvitationCode(Long userId) {
        // 检查用户是否已经存在未使用的邀请码
        Optional<InvitationCode> existingCode = invitationCodeRepository.findByUserIdAndUsed(userId, false);
        return existingCode.orElseGet(() -> generateNewInvitationCode(userId));
    }

    public InvitationCode getInvitationCode(Long userId) {
        // 检查用户是否已经存在未使用的邀请码
        Optional<InvitationCode> existingCode = invitationCodeRepository.findByUserIdAndUsed(userId, false);
        return existingCode.orElse(null);
    }

    /**
     * 生成新的邀请码
     * @param userId 用户 ID
     * @return 新的邀请码对象
     */
    private InvitationCode generateNewInvitationCode(Long userId) {
        // 生成随机邀请码
        String code = generateUniqueCode();

        // 创建新的邀请码对象
        InvitationCode newCode = new InvitationCode();
        newCode.setCode(code);
        newCode.setUsed(false);
        newCode.setUserId(userId);

        // 保存到数据库
        return invitationCodeRepository.save(newCode);
    }

    /**
     * 生成唯一的邀请码（包含时间戳）
     * @return 唯一的邀请码
     * @throws RuntimeException 如果经过多次重试仍无法生成唯一的邀请码
     */
    private String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZqwertyuioplkjhgfdsazxcvbnm0123456789";
        StringBuilder code = new StringBuilder();
        final int MAX_RETRIES = 3; // 最大重试次数
        int retryCount = 0; // 当前重试次数

        // 生成随机字符部分
        for (int i = 0; i < 26; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }

        // 添加时间戳部分
        String timestamp = String.valueOf(System.currentTimeMillis());
        code.append(timestamp);

        // 检查生成的邀请码是否已存在，如果存在则重新生成
        while (invitationCodeRepository.findByCode(code.toString()) != null) {
            retryCount++;
            if (retryCount > MAX_RETRIES) {
                throw new RuntimeException("经过多次重试仍无法生成唯一的邀请码");
            }
            code.setLength(0);
            for (int i = 0; i < 26; i++) {
                code.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            code.append(timestamp);
        }

        return code.toString();
    }
}