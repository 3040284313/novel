package com.wtl.novel.Service;

import com.wtl.novel.entity.Terminology;
import com.wtl.novel.repository.TerminologyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TerminologyService {

    @Autowired
    private TerminologyRepository terminologyRepository;

    public Terminology save(Terminology save) {
        return terminologyRepository.save(save);
    }

    public List<Terminology> findAllByNovelId(Long novelId) {
        List<Terminology> list = terminologyRepository.findAllByNovelId(novelId);
        List<Terminology> allByNovelId = terminologyRepository.findAllByNovelId(0L);
        list.addAll(allByNovelId);
        return list;
    }

    public Set<Integer> findDistinctChapterNumbersByNovelId(Long novelId) {
        Set<Integer> set = new HashSet<>(terminologyRepository.findDistinctChapterNumbersByNovelId(0L));
        set.addAll(terminologyRepository.findDistinctChapterNumbersByNovelId(novelId));
        return set;
    }

    public Set<Integer> findDistinctChapterNumbersByNovelIdAndSourceTargetDownloaded(Long novelId) {
        return terminologyRepository.findDistinctChapterNumbersByNovelIdAndSourceTargetDownloaded(novelId,"已下载","已下载");
    }

    // 根据 chapter_true_id 列表查询数据，并返回去重后的 chapter_true_id
    public Set<String> findDistinctChapterTrueIdsByChapterTrueIdIn(List<String> chapterTrueIds) {
        return terminologyRepository.findDistinctChapterTrueIdsByChapterTrueIdIn(chapterTrueIds);
    }

    // 根据 novel_id 分页查询
    public Page<Terminology> findByNovelId(Long novelId, Pageable pageable) {
        return terminologyRepository.findByNovelId(novelId, pageable);
    }

    public Page<Terminology> getTerminologyByPlatform(Long novelId, Pageable pageable) {
        return terminologyRepository.findByNovelId(novelId, pageable);
    }
}