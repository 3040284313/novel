package com.wtl.novel.util;

import org.mindrot.jbcrypt.BCrypt;

public class CustomPasswordEncoder {

    /**
     * 对密码进行加密
     */
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}