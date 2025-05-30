package com.wtl.novel.Interceptor;


import com.wtl.novel.Service.ChapterService;
import com.wtl.novel.util.SignatureUtils;
import com.wtl.novel.util.URLMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

import static com.wtl.novel.Service.RequestLogService.CHAPTERS_ID_PATTERN_AND_GET_ID;
import static com.wtl.novel.util.URLMatcher.matches;


@Component
public class SignatureInterceptor implements HandlerInterceptor {

    @Autowired
    private ChapterService chapterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String requestURI = request.getRequestURI();
            if (matches(requestURI)) {
                return true;
            }

//            Matcher matcher = CHAPTERS_ID_PATTERN_AND_GET_ID.matcher(requestURI);
//            if (matcher.matches()) {
//                String id = matcher.group(1); // 获取第一个括号中的内容
//                Integer chapterNumber = chapterService.findChapterNumberById(Long.parseLong(id));
//                if (chapterNumber != null && chapterNumber < 50) {
//                    return true;
//                }
//            }

            // 检查 Authorization 头是否存在
            String authorizationInfo = request.getHeader("authorization");
            if (authorizationInfo == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing authorization header");
                return false;
            }

            // 解析 Authorization 头
            String[] authorizationInfos = authorizationInfo.split(";");
            if (authorizationInfos.length < 4) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid authorization format");
                return false;
            }

            // 提取签名参数
            String authorization = authorizationInfos[0];
            String signature = authorizationInfos[1];
            String timestamp = authorizationInfos[2];
            Instant instant = Instant.now();
            long nowTimestamp = instant.toEpochMilli();
            long compareTimestamp = nowTimestamp - Long.parseLong(timestamp);
            if (Math.abs(compareTimestamp) >= 60000) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature");
                return false;
            }
            String nonce = authorizationInfos[3];

            // 验证签名
            boolean valid = SignatureUtils.validateSignature(
                    signature, timestamp, nonce,
                    request.getMethod(), request.getRequestURI(),
                    URLMatcher.matchesISValid(requestURI) ? new HashMap<>() : getParams(request), URLMatcher.matchesISValid(requestURI) ? null : getDataFromRequest(request),
                    authorization
            );

            if (!valid) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature");
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Error: " + e.getMessage());
            return false;
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
        response.getWriter().flush();
        response.getWriter().close();
    }

    private Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String> params = new TreeMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    /**
     * 从请求中获取数据
     *
     * @param request HTTP 请求
     * @return 请求体数据
     */
    private String getDataFromRequest(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            char[] buffer = new char[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
        } catch (IOException e) {
            // 记录日志或处理异常
            e.printStackTrace();
        }
        return sb.toString();
    }



}