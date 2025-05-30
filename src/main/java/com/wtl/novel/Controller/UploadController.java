package com.wtl.novel.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.CDO.*;
import com.wtl.novel.Service.*;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.User;
import com.wtl.novel.entity.UserNovelRelation;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.util.CloudflareR2Util;
import com.wtl.novel.util.DiskSpaceUtils;
import jakarta.persistence.Access;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/share")
public class UploadController {
    
    @Autowired
    private UploadService uploadService;
    @Autowired
    private UserService userService;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private UserNovelRelationService userNovelRelationService;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @PostMapping(
            path    = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> upload(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "cover", required = false) MultipartFile cover,
            @RequestPart("metadata") UploadMetadata metadata,
            HttpServletRequest httpRequest) {
        boolean executeTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadNovel").getValueField());
        if (!executeTr) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (DiskSpaceUtils.isSpaceLessThan3GB()) {
            return ResponseEntity.status(500).body("存储空间不足");
        }
        if (metadata.getTags().isEmpty()) {
            return ResponseEntity.status(500).body("未上传标签");
        }
        // 检查是否已经有任务在处理该 keyword
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body("用户未登录");
        }
        if (modifyUserStatue(credential.getUser().getId())) {
            return ResponseEntity.status(500).body("当前存在正在处理的文件");
        }
        userService.updateUploadByUserId(credential.getUser().getId(), true);
        try {
            uploadService.upload(file, cover, metadata, credential.getUser().getId());
            // 这里可以访问 metadata 对象中的所有字段
            System.out.println("Type: " + metadata.getType());
            System.out.println("Original Title: " + metadata.getOriginalTitle());
            System.out.println("Work Title: " + metadata.getWorkTitle());
            System.out.println("Tags: " + metadata.getTags());

            return ResponseEntity.ok("上传成功！");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("上传失败：" + e.getMessage());
        }
    }


    @PostMapping(
            path    = "/update",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> update(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("metadata") UpdateMetadata metadata,
            HttpServletRequest httpRequest) {
        boolean executeTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadNovel").getValueField());
        if (!executeTr) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // 检查是否已经有任务在处理该 keyword
        if (DiskSpaceUtils.isSpaceLessThan3GB()) {
            return ResponseEntity.status(500).body("存储空间不足");
        }
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body("用户未登录");
        }
        if (modifyUserStatue(credential.getUser().getId())) {
            return ResponseEntity.status(500).body("当前存在正在处理的文件");
        }
        userService.updateUploadByUserId(credential.getUser().getId(), true);
        List<UserNovelRelation> upload = userNovelRelationService.getNovelDetail(metadata.getNovelId(), "upload", credential.getUser().getId());
        if (upload.isEmpty()) {
            return ResponseEntity.status(500).body("无权操作改内容");
        }
        try {
            uploadService.update(file, metadata.getNovelId(), metadata.getType(), credential.getUser().getId());
            return ResponseEntity.ok("上传成功！");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("上传失败：" + e.getMessage());
        }
    }


    @PostMapping("/updateNovelDetail/{id}" )
    public ResponseEntity<UploadNovelDTO> updateNovelDetail(@PathVariable Long id,
                                                            @RequestPart(value = "cover", required = false) MultipartFile cover,
                                                            @RequestPart("metadata") UploadMetadata metadata,
                                                            HttpServletRequest httpRequest) {
        boolean executeTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadNovel").getValueField());
        if (!executeTr) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (DiskSpaceUtils.isSpaceLessThan3GB()) {
            return ResponseEntity.status(500).body(null);
        }
        // 检查是否已经有任务在处理该 keyword
        if (metadata.getTags().isEmpty()) {
            return ResponseEntity.status(500).body(null);
        }
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body(null);
        }
        if (modifyUserStatue(credential.getUser().getId())) {
            return ResponseEntity.status(500).body(null);
        }
        userService.updateUploadByUserId(credential.getUser().getId(), true);
        try {
            UploadNovelDTO upload = uploadService.updateNovelDetail(cover, metadata, id, credential.getUser().getId());
            return ResponseEntity.ok(upload);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    @PostMapping("/uploadChapter/{id}")
    public ResponseEntity<Boolean> saveChapter(@PathVariable("id") Long id, @RequestBody ChapterRequest chapterRequest,HttpServletRequest httpRequest) {
        boolean executeTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadNovel").getValueField());
        if (!executeTr) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (DiskSpaceUtils.isSpaceLessThan3GB()) {
            return ResponseEntity.status(500).body(false);
        }
        if (chapterRequest.getContent().length() > 30000 || chapterRequest.getTitle().length() > 500) {
            return ResponseEntity.status(500).body(false);
        }
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body(false);
        }
        if (modifyUserStatue(credential.getUser().getId())) {
            return ResponseEntity.status(500).body(false);
        }
        userService.updateUploadByUserId(credential.getUser().getId(), true);
        ChapterCDO chapterById = chapterService.findChapterById(id, credential.getUser().getId());
        List<UserNovelRelation> upload = userNovelRelationService.getNovelDetail(chapterById.getNovelId(), "upload", credential.getUser().getId());
        if (upload.isEmpty()) {
            return ResponseEntity.status(500).body(false);
        }
        // 这里是处理请求的逻辑
        // 例如，调用服务层来处理数据保存
        uploadService.saveChapter(credential.getUser().getId(),id,chapterRequest.getTitle(), chapterRequest.getContent(), chapterRequest.getImgInfo());
        return ResponseEntity.ok(true);
    }

    public boolean modifyUserStatue(Long userId) {
        User user = userService.findUserById(userId);
        return user.getUpload();
    }




    @GetMapping("/getUpload" )
    public ResponseEntity<List<Novel>> getUpload(HttpServletRequest httpRequest) {
        // 检查是否已经有任务在处理该 keyword
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body(null);
        }
        try {
            List<Novel> upload = uploadService.getUpload(credential.getUser().getId());
            return ResponseEntity.ok(upload);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping("/getNovelDetail/{id}" )
    public ResponseEntity<UploadNovelDTO> getNovelDetail(@PathVariable Long id, HttpServletRequest httpRequest) {
        // 检查是否已经有任务在处理该 keyword
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body(null);
        }
        try {
            UploadNovelDTO upload = uploadService.getNovelDetail(id, credential.getUser().getId());
            return ResponseEntity.ok(upload);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    // 创建一个 DTO 类来封装非文件参数
    @GetMapping("/newChapter/{novelId}/{type}")
    public ResponseEntity<Long> newChapter(@PathVariable("novelId") Long novelId, @PathVariable("type") Integer type,HttpServletRequest httpRequest) {
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
        return ResponseEntity.ok(uploadService.newChapter(novelId, type));
    }

}