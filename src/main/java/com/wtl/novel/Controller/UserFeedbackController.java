package com.wtl.novel.Controller;

import com.wtl.novel.Service.ChapterService;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.UserFeedbackService;
import com.wtl.novel.entity.Chapter;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.UserFeedback;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@RequestMapping("/api/feedback")
public class UserFeedbackController {
    @Autowired
    private UserFeedbackService userFeedbackService;
    @Autowired
    private CredentialService credentialService;

    @PostMapping("/add")
    public ResponseEntity<String> createFeedback(@RequestBody UserFeedback feedback, HttpServletRequest httpRequest) {
        if(feedback.getContent().length()>5000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("内容太长");
        }
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null) {
            return null;
        }
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        List<UserFeedback> byNovelIdAndChapterId = userFeedbackService.findByNovelIdAndChapterIdAndIsDeleteFalse(feedback.getNovelId(), feedback.getChapterId());
        if (!byNovelIdAndChapterId.isEmpty()) {
            return ResponseEntity.ok("已经有人提交");
        }
        feedback.setUserId(credential.getUser().getId());
        UserFeedback feedback1 = userFeedbackService.createFeedback(feedback);
        return ResponseEntity.ok("提交成功");
    }


    @GetMapping("/status")
    public Page<UserFeedback> getFeedbacksByStatus(
            @RequestParam Boolean isResolved,
            @RequestParam int page,
            @RequestParam int size) {
        return userFeedbackService.getFeedbacksByStatus(isResolved, page, size);
    }

}