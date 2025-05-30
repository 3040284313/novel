package com.wtl.novel.Controller;

import com.wtl.novel.CDO.ChapterCDO;
import com.wtl.novel.CDO.ChapterUpdateCTO;
import com.wtl.novel.DTO.ChapterProjection;
import com.wtl.novel.Service.*;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.UserRepository;
import com.wtl.novel.util.SignatureUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ChapterUpdateService chapterUpdateService;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private UserNovelRelationService userNovelRelationService;

    @GetMapping("/{id}")
    public ChapterCDO getChapterById(@PathVariable Long id, HttpServletRequest httpRequest) throws Exception {
        String authorization = httpRequest.getHeader("Authorization");
//        if (authorization == null) {
//            return chapterService.findChapterById(id,null);
//        }
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return null;
        }
//        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
//            return chapterService.findChapterById(id,null);
//        }
        ChapterCDO chapterById = chapterService.findChapterById(id, credential.getUser().getId());
        chapterById.setContent(SignatureUtils.encrypt(chapterById.getContent(), credential.getToken()));
        return chapterById;
    }

    @GetMapping("/upload/{id}")
    public ResponseEntity<ChapterCDO> getUploadChapterById(@PathVariable Long id, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body(null);
        }
        ChapterCDO chapterById = chapterService.findChapterById(id, credential.getUser().getId());
        List<UserNovelRelation> upload = userNovelRelationService.getNovelDetail(chapterById.getNovelId(), "upload", credential.getUser().getId());
        if (upload.isEmpty()) {
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok(chapterById);
    }

    @GetMapping("/novel/{novelId}")
    public List<Long> findIdsByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(@PathVariable Long novelId) {
        return chapterService.findIdsByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(novelId);
    }

    @GetMapping("/novel/{novelId}/page/{page}/{size}")
    public Page<ChapterProjection> getChaptersByNovelIdAndPage(@PathVariable Long novelId, @PathVariable int page, @PathVariable int size) {
        return chapterService.getChaptersByNovelIdWithPagination(novelId, page, size);
    }

    @GetMapping("/getChaptersByNovelId/{novelId}")
    public List<ChapterProjection> getChaptersByNovelId(@PathVariable Long novelId) {
        return chapterService.getChaptersByNovelId(novelId);
    }

    @GetMapping("/getUploadChaptersByNovelId/{novelId}")
    public ResponseEntity<List<ChapterProjection>> getUploadChaptersByNovelId(@PathVariable Long novelId, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body(null);
        }
        List<UserNovelRelation> upload = userNovelRelationService.getNovelDetail(novelId, "upload", credential.getUser().getId());
        if (upload.isEmpty()) {
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok(chapterService.getChaptersByNovelId(novelId));
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateChapterContent(@RequestBody ChapterUpdate request, HttpServletRequest httpRequest) {
        // 检查内容是否为空或仅包含空白字符
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("提交失败：内容不能为空");
        }
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("提交失败");
        }

        request.setUserId(credential.getUser().getId());

        Chapter chapter = chapterService.findChapterByChapterId(request.getChapterId());
        if (chapter == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("提交失败");
        }

        String content = chapter.getContent();
        int length = request.getContent().length();
        if (content.length() * 2 < length) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("提交失败");
        }

        // 清除不必要的字段
        request.setId(null);
        request.setUpdatedAt(null);
        request.setCreatedAt(null);

        // 调用服务层方法
        boolean success = chapterUpdateService.updateChapterContent(request);

        // 根据操作结果返回不同的响应
        if (success) {
            return ResponseEntity.ok("提交成功，待审核");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("提交失败");
        }
    }
}