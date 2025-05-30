package com.wtl.novel.Service;

import com.wtl.novel.entity.Tag;
import com.wtl.novel.repository.NovelTagRepository;
import com.wtl.novel.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NovelTagRepository novelTagRepository;
    
    public List<Tag> save(List<Tag> tags) {
        if (tags.isEmpty()) {
            return tags;
        }
        List<Tag> byPlatformAndNameIn = tagRepository.findByPlatformAndNameIn(tags.get(0).getPlatform(), tags.stream().map(Tag::getName).collect(Collectors.toList()));
        List<Tag> tagList = new ArrayList<>(byPlatformAndNameIn);

        // 从已有标签中移除已存在的标签
        List<Tag> tagsToSave = tags.stream()
                .filter(tag -> byPlatformAndNameIn.stream().noneMatch(existingTag -> existingTag.getName().equals(tag.getName())))
                .collect(Collectors.toList());

        // 保存不存在的标签
        List<Tag> savedTags = tagRepository.saveAll(tagsToSave);

        // 将已存在的标签和新保存的标签合并
        tagList.addAll(savedTags);

        return tagList;
    }

    public List<Tag> getAllTags() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return tagRepository.findAll(sort);
    }
    public List<Tag> getAllTagsByPlatform(String platform) {
        Pageable pageable = PageRequest.of(0, 1500);
        // tagRepository.findByPlatform(platform, Sort.by(Sort.Direction.ASC, "id"));
        return tagRepository.findByPlatformLimit(platform, pageable);
    }

    public List<Tag> findByPlatformAnd(String platform, String keyword) {
        return tagRepository.findByPlatformAnd(platform, keyword);
    }

    // 根据 novelId 获取所有 Tag 对象
    public List<String> getTagsByNovelId(Long novelId) {
        // 第一步：根据 novelId 查询所有 tagId
        List<Long> tagIds = novelTagRepository.findTagIdsByNovelId(novelId);

        // 第二步：根据 tagId 列表查询 Tag 对象
        return tagRepository.findByIdIn(tagIds).stream().map(Tag::getName).collect(Collectors.toList());
    }

    // 根据 novelId 获取所有 Tag 对象
    public List<Tag> getTagsAllInfoByNovelId(Long novelId) {
        // 第一步：根据 novelId 查询所有 tagId
        List<Long> tagIds = novelTagRepository.findTagIdsByNovelId(novelId);

        // 第二步：根据 tagId 列表查询 Tag 对象
        return tagRepository.findByIdIn(tagIds);
    }
}
