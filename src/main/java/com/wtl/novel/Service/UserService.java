package com.wtl.novel.Service;

import com.wtl.novel.entity.Post;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.PostRepository;
import com.wtl.novel.repository.UserRepository;
import com.wtl.novel.util.CustomPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomPasswordEncoder passwordEncoder;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private DictionaryRepository dictionaryRepository;

    public boolean deductPoints(Long userId, Long points) {
        return userRepository.checkAndDecreasePoints(userId, points);
    }

    public int updateUploadByUserId(Long userId, Boolean upload) {
        return userRepository.updateUploadByUserId(userId, upload);
    };

    @Transactional
    public boolean rewardPoints(Long postId, Long fromUserId, Long points) {
        // 检查打赏积分是否有效
        if (points <= 0) {
            throw new IllegalArgumentException("打赏积分必须大于 0");
        }
        Post post = postRepository.findById(postId).get();
        Long toUserId = post.getUserId();
        if (fromUserId.equals(toUserId)) {
            return true;
        }
        // 查询打赏用户
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("打赏用户不存在"));

        // 查询接收用户
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("接收用户不存在"));

        // 检查打赏用户是否有足够的积分
        if (fromUser.getPoint() < points) {
            throw new RuntimeException("打赏用户积分不足");
        }

        // 扣除打赏用户的积分
        fromUser.setPoint(fromUser.getPoint() - points);

        // 增加接收用户的积分
        toUser.setPoint(toUser.getPoint() + points);

        // 保存两个用户
        userRepository.save(fromUser);
        userRepository.save(toUser);

        return true;
    }

    public User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPoint(0L);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findUserById(Long userId) {
        return userRepository.findUserById(userId);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Long getUserPoint(Long userId) {
        return userRepository.findPointByUserId(userId);
    }
}