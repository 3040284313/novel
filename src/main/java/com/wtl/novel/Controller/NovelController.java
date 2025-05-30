package com.wtl.novel.Controller;

import com.wtl.novel.CDO.NovelCTO;
import com.wtl.novel.CDO.NovelSearchRequest;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.NovelService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Favorite;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;


@RestController
@RequestMapping("/api/novels")
public class NovelController {

    @Autowired
    private NovelService novelService;
    @Autowired
    private CredentialService credentialService;


    @GetMapping("/tag/{tagId}")
    public Page<Novel> getNovelsByTagId(@PathVariable Long tagId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return novelService.getNovelsByTagIdWithPagination(tagId, pageable);
    }

    @GetMapping("/get")
    public Page<Novel> getNovelsWithPagination(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return novelService.getNovelsWithPagination(page, size);
    }

    // 获取小说标签列表
    @GetMapping("/getTags/{novelId}")
    public List<Tag> getTagsByNovelId(@PathVariable Long novelId) {
        return novelService.getTagsByNovelId(novelId);
    }

    // 增加点赞数
    @PutMapping("/{id}/{type}/{favoriteType}/{groupId}")
    public ResponseEntity<Integer> increaseUp(@PathVariable Long id,
                                              @PathVariable String type,
                                              @PathVariable String favoriteType,
                                              @PathVariable Long groupId,
                                              HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(0);
        }
        return ResponseEntity.ok(novelService.increaseUp(id, credential.getUser(), type, favoriteType, groupId));
    }

    // 获取特定小说的信息
    @GetMapping("/{id}")
    public NovelCTO getNovelById(@PathVariable Long id, HttpServletRequest httpRequest) {
        return novelService.findNovelById(id, httpRequest);
    }

    // 根据tag分页查询小说
//    @GetMapping("/page/{tagId}/{page}/{size}")
//    public Page<Novel> getNovels(@PathVariable Long tagId,@PathVariable int page, @PathVariable int size) {
//        return novelService.getNovelsWithPagination(tagId, page, size);
//    }

    @GetMapping("/searchByKeyWord")
    public Page<Novel> search(@RequestParam String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return novelService.findByTitleContainingOrTrueNameContaining(keyword);
        }
        return Page.empty();
    }


//    ====

//    // 根据tag分页查询小说
//    @GetMapping("/getNovelsByPlatform/{platform}/{tabId}/{fontNumber}/{page}/{size}")
//    public Page<Novel> getNovelsByPlatform(@PathVariable String platform,@PathVariable Long tabId,@PathVariable String fontNumber,@PathVariable int page, @PathVariable int size) {
//        return novelService.getNovelsWithPagination(platform, fontNumber,tabId,page, size);
//    }

    // 根据平台和标签分页查询小说
    @PostMapping("/getNovelsByPlatform")
    public Page<NovelCTO> getNovelsByPlatform(
            @RequestBody NovelSearchRequest request,HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        // 创建分页请求
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // 调用服务层方法
        return novelService.getNovelsWithPagination(
                request.getPlatform(),
                request.getFontNumber(),
                request.getTabIds(),
                pageable,credential.getUser().getId());
    }

}