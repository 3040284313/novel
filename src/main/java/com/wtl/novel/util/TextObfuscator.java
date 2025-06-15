package com.wtl.novel.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TextObfuscator {
    private static final char[] ZERO_WIDTH_CHARS = {
            '\u200B', // 零宽空格
            '\u200D', // 零宽连接符
            '\uFEFF'  // 零宽非断空格
    };

    private static final Random random = new Random();

    /**
     * 混淆文本（添加零宽字符和随机换行）
     */
    public static String obfuscateText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i]);

            if (i < chars.length - 1) {
                // 随机插入1-3个零宽字符
                int count = 1 + random.nextInt(3);
                for (int j = 0; j < count; j++) {
                    char zc = ZERO_WIDTH_CHARS[random.nextInt(ZERO_WIDTH_CHARS.length)];
                    sb.append(zc);
                }

                // 20%概率插入换行符
                if (random.nextDouble() < 0.2) {
                    sb.append('\n');
                }
            }
        }

        return sb.toString();
    }

    /**
     * 将混淆文本随机插入到多行文本中
     */
    public static String insertObfuscatedText(String originalContent, String fixedText) {
        // 混淆固定文本
        String obfuscatedText = obfuscateText(fixedText);

        // 分割原始文本为多行
        String[] lines = originalContent.split("\n");
        List<String> lineList = new ArrayList<>(List.of(lines));

        // 随机选择插入位置（0 到 行数之间）
        int insertPosition = random.nextInt(lineList.size() + 1);

        // 插入混淆文本
        lineList.add(insertPosition, obfuscatedText);

        // 重新组合为多行文本
        return String.join("\n", lineList);
    }
}