package com.wtl.novel.Service;

import com.wtl.novel.entity.NovelTag;
import com.wtl.novel.repository.NovelTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NovelTagService {

    @Autowired
    private NovelTagRepository novelTagRepository;

    // 根据 novelId 分页查询 tagId 列表
    public List<NovelTag> findTagIdsByNovelId(Long novelId) {
        return novelTagRepository.findByNovelId(novelId);
    }

    public List<NovelTag> save(List<NovelTag> novelTagList) {
        List<NovelTag> toSave = new ArrayList<>();
        for (NovelTag novelTag : novelTagList) {
            if (novelTagRepository.findByNovelIdAndTagId(novelTag.getNovelId(), novelTag.getTagId()).isEmpty()) {
                toSave.add(novelTag);
            }
        }
        return novelTagRepository.saveAll(toSave);
    }
}