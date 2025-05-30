package com.wtl.novel.Controller;

import com.wtl.novel.CDO.PostCTO;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.PostService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Post;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private CredentialService credentialService;

    @GetMapping("/getPosts")
    public ResponseEntity<Page<PostCTO>> getAllPosts(
            @RequestParam(required = false) Integer postType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // 创建分页和排序对象
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.fromString(sortDirection),
                sortBy
        );

        return ResponseEntity.ok(postService.getPostsByPostType(postType, pageable));
    }

    @GetMapping("/getAllPostsByNovelId")
    public ResponseEntity<Page<PostCTO>> getAllPostsByNovelId(
            @RequestParam Long novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // 创建分页和排序对象
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.fromString(sortDirection),
                sortBy
        );

        return ResponseEntity.ok(postService.getAllPostsByNovelId(novelId, pageable));
    }



    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id, HttpServletRequest httpRequest) {
        Post post = postService.getPostById(id);
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null) {
            post.setAuthor(null);
            post.setUserId(null);
            post.setCollections(null);
            post.setCommentNum(null);
            post.setPostType(null);
            return ResponseEntity.ok(post);
        }
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            post.setAuthor(null);
            post.setUserId(null);
            post.setCollections(null);
            post.setCommentNum(null);
            post.setPostType(null);
            return ResponseEntity.ok(post);
        }
        if (post != null) {
            return ResponseEntity.ok(post);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/createPost")
    public ResponseEntity<Post> createPost(@RequestBody Post post, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        post.setUserId(credential.getUser().getId());
        post.setAuthor(credential.getUser().getEmail());
        post.setCommentNum(0);
        Post createdPost = postService.createPost(post);
        return ResponseEntity.status(201).body(createdPost);
    }

}