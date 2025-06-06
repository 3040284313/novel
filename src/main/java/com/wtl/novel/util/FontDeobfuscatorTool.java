package com.wtl.novel.util;

import org.apache.fontbox.ttf.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FontDeobfuscatorTool {


    private static final Map<Integer, Integer> FAKE_TO_REAL_MAP = new HashMap<>();

    // 初始化时加载字体映射关系
    static {
        try {
            // 1. 字体文件路径配置
            String targetDirectoryBasedOnOS = CloudflareR2Util.getTargetDirectoryBasedOnOS1();
            String standardFontPath = targetDirectoryBasedOnOS + "NotoSansKR-VariableFont_wght.ttf";
            String obfuscatedFontPath = targetDirectoryBasedOnOS + "obfuscatedNotoSansKR2.ttf";

            // 2. 解析标准字体和混淆字体
            TTFParser parser = new TTFParser();
            TrueTypeFont standardFont = parser.parse(new File(standardFontPath));
            TrueTypeFont obfuscatedFont = parser.parse(new File(obfuscatedFontPath));

            // 3. 构建映射表
            Map<Integer, Integer> standardGlyphToCode = parseCmap(standardFont.getCmap());
            Map<Integer, Integer> obfuscatedGlyphToCode = parseCmap(obfuscatedFont.getCmap());

            // 4. 建立混淆Unicode到真实Unicode的映射
            for (Map.Entry<Integer, Integer> entry : obfuscatedGlyphToCode.entrySet()) {
                int glyphId = entry.getKey();
                int fakeCode = entry.getValue();
                if (standardGlyphToCode.containsKey(glyphId)) {
                    FAKE_TO_REAL_MAP.put(fakeCode, standardGlyphToCode.get(glyphId));
                }
            }

            // 5. 关闭字体资源
            standardFont.close();
            obfuscatedFont.close();

        } catch (IOException e) {
            throw new RuntimeException("字体文件加载失败", e);
        }
    }

    /**
     * 将混淆字符串转换为真实文字
     */
    public static String deobfuscate(String obfuscatedText) {
        StringBuilder realText = new StringBuilder();
        for (char c : obfuscatedText.toCharArray()) {
            int realCode = FAKE_TO_REAL_MAP.getOrDefault((int) c, (int) c);
            realText.append((char) realCode);
        }
        return realText.toString();
    }

    /**
     * 解析cmap表（兼容FontBox 2.0.28+）
     */
    private static Map<Integer, Integer> parseCmap(CmapTable cmap) {
        Map<Integer, Integer> glyphToCode = new HashMap<>();
        if (cmap == null) return glyphToCode;

        // 遍历所有子表，选择Windows Unicode子表（PlatformID=3, EncodingID=1）
        for (CmapSubtable subtable : cmap.getCmaps()) {
            if (subtable.getPlatformId() == 3 && subtable.getPlatformEncodingId() == 1) {
                // 遍历BMP范围字符（0x0000 - 0xFFFF）
                for (int codePoint = 0; codePoint <= 0xFFFF; codePoint++) {
                    int glyphId = subtable.getGlyphId(codePoint);
                    if (glyphId > 0) {
                        glyphToCode.put(glyphId, codePoint);
                    }
                }
                break;
            }
        }
        return glyphToCode;
    }
}