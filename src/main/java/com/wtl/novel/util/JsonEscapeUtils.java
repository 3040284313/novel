package com.wtl.novel.util;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonEscapeUtils {
    private static final Map<Character, String> ESCAPE_MAP = new HashMap<>();

    static {
        // 定义需要转义的字符及其对应的JSON转义序列
        ESCAPE_MAP.put('"', "\\\"");
        ESCAPE_MAP.put('\\', "\\\\");
        ESCAPE_MAP.put('/', "\\/");
        ESCAPE_MAP.put('\b', "\\b");
        ESCAPE_MAP.put('\f', "\\f");
        ESCAPE_MAP.put('\n', "\\n");
        ESCAPE_MAP.put('\r', "\\r");
        ESCAPE_MAP.put('\t', "\\t");
    }

    public static String escapeJsonString(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\"","＂");
    }

    public static String escapeJsonString1(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (ESCAPE_MAP.containsKey(c)) {
                // 处理已知转义字符
                sb.append(ESCAPE_MAP.get(c));
            } else if (c < 0x20) {
                // 处理其他控制字符（ASCII < 0x20）
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                // 无需转义的字符
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Map<String, String> getTable(String json) {
        // 第一步：提取table对象内容的正则
        // 改进后的正则表达式
        String tableRegex =
                "\"table\"\\s*:\\s*\\{([\\s\\S]*?)\\}\\s*(?=,?\\s*(\"translation\"|\\}))";
        Pattern pattern = Pattern.compile(tableRegex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        Map<String, String> tableMap = new HashMap<>();
        if (matcher.find()) {
            // 修复提取的JSON片段
            String tableContent = matcher.group(1).trim();
            String validJson = "{ " + tableContent + " }"; // 包裹成完整对象

            // 转换为Map
            Gson gson = new Gson();
            tableMap = gson.fromJson(validJson,
                    new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
        }
        return tableMap;
    }

    public static String getTranslation1(String json) {
        // 正则匹配 "translation": "..." 到最后一个 } 之间的内容
        Pattern pattern = Pattern.compile(
                "\"translation\"\\s*:\\s*\"([^\"]*)\"\\s*\\}"  // 匹配 "translation": "..." }
        );
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            // 提取内容并处理转义字符
            return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        }
        return ""; // 未匹配到内容时返回空字符串
    }

    public static String getTranslation(String json) {
        // 正则表达式：匹配从"translation":到最后一个}之间的内容
        String regex =
                "\"translation\"\\s*:\\s*\" " + // 匹配键和冒号
                        "((?s).*?)" +                   // 匹配所有字符（包括换行）
                        "\"\\s*}" +                     // 匹配结束引号和对象闭合符
                        "(?=\\s*$)" ;                   // 确保是JSON的最后一个}

        Pattern pattern = Pattern.compile(regex, Pattern.COMMENTS);
        Matcher matcher = pattern.matcher(json);
        StringBuffer sb = new StringBuffer();
        if (matcher.find()) {
            // 提取内容并还原转义字符
            String content = matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
            sb.append(content);
        }
        String string = sb.toString();
        if (string.isEmpty()) {
            string = JsonEscapeUtils.getTranslation1(json);
        }
        return string;
    }

}