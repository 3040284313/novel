package com.wtl.novel.Service;

import com.wtl.novel.CDO.PostCTO;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.Post;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private NovelRepository novelRepository;

    public void incrementCommentNum(Long postId) {
        postRepository.incrementCommentNumById(postId);
    }

    public Page<PostCTO> getPostsByPostType(Integer postType, Pageable pageable) {
        Page<Post> byPostType = postRepository.findByPostType(postType, pageable);

        // 提取 Post 的 collections 字段
        List<Long> idList = byPostType.getContent().stream()
                .map(Post::getCollections)
                .collect(Collectors.toList());

        // 查询对应的 Novel
        List<Novel> novels = novelRepository.findByIdInAndIsDeletedFalse(idList);

        // 将 Post 转换为 PostCTO
        List<PostCTO> postCTOs = byPostType.getContent().stream()
                .map(post -> {
                    // 找到对应的 Novel
                    Novel novel = novels.stream()
                            .filter(n -> n.getId().equals(post.getCollections()))
                            .findFirst()
                            .orElse(null);

                    // 创建 PostCTO
                    return new PostCTO(post, novel != null ? novel.getTitle() : "无名");
                })
                .collect(Collectors.toList());

        // 创建新的 Page<PostCTO>
        return new PageImpl<>(postCTOs, pageable, byPostType.getTotalElements());
    }

    public Page<PostCTO> getAllPostsByNovelId(Long novelId, Pageable pageable) {
        Page<Post> byPostType = postRepository.findByPostTypeAndNovelId(novelId, pageable);

        // 提取 Post 的 collections 字段
        List<Long> idList = new ArrayList<>();
        idList.add(novelId);

        // 查询对应的 Novel
        List<Novel> novels = novelRepository.findByIdInAndIsDeletedFalse(idList);

        // 将 Post 转换为 PostCTO
        List<PostCTO> postCTOs = byPostType.getContent().stream()
                .map(post -> {
                    // 找到对应的 Novel
                    Novel novel = novels.stream()
                            .filter(n -> n.getId().equals(post.getCollections()))
                            .findFirst()
                            .orElse(null);

                    // 创建 PostCTO
                    return new PostCTO(post, novel != null ? novel.getTitle() : "无名");
                })
                .collect(Collectors.toList());

        // 创建新的 Page<PostCTO>
        return new PageImpl<>(postCTOs, pageable, byPostType.getTotalElements());
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public PostCTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null && post.getCollections() != null) {
            Novel byIdAndIsDeletedFalse = novelRepository.findByIdAndIsDeletedFalse(post.getCollections());
            return new PostCTO(post, byIdAndIsDeletedFalse.getTitle());
        }
        assert post != null;
        return new PostCTO(post, "");
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post collectPost(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setCollections(post.getCollections() + 1);
            return postRepository.save(post);
        }
        return null;
    }

}