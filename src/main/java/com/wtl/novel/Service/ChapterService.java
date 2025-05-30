package com.wtl.novel.Service;

import com.wtl.novel.CDO.ChapterCDO;
import com.wtl.novel.DTO.ChapterProjection;
import com.wtl.novel.entity.Chapter;
import com.wtl.novel.entity.ChapterImageLink;
import com.wtl.novel.entity.ChapterUpdate;
import com.wtl.novel.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private ReadingRecordService readingRecordService;
    @Autowired
    private ChapterImageLinkService chapterImageLinkService;

    public void delete(Long chapterId) {
        chapterRepository.deleteById(chapterId);
    }

    public ChapterCDO findChapterById(Long id,Long userId) {
        Chapter byIdAndIsDeletedFalse = chapterRepository.findByIdAndIsDeletedFalse(id);
        ChapterCDO chapterCDO = new ChapterCDO(byIdAndIsDeletedFalse);
        if (byIdAndIsDeletedFalse.isOwnPhoto()) {
            List<ChapterImageLink> chapterImageLinkList = chapterImageLinkService.findByChapterTrueId(byIdAndIsDeletedFalse.getTrueId());

            Set<String> existingContentLinkLocationPairs = new HashSet<>();
            List<ChapterImageLink> duplicatesToDelete = new ArrayList<>();
            List<ChapterImageLink> uniqueChapterImageLinkList = new ArrayList<>();

            for (ChapterImageLink current : chapterImageLinkList) {
                String contentLinkLocationPair = current.getContentLink() + "-" + current.getLocation();

                // 如果这对组合已经存在于集合中，说明是重复数据，需要删除
                if (existingContentLinkLocationPairs.contains(contentLinkLocationPair)) {
                    duplicatesToDelete.add(current);
                } else {
                    // 如果这对组合不存在于集合中，添加到集合中，并将当前对象添加到保留列表中
                    existingContentLinkLocationPairs.add(contentLinkLocationPair);
                    uniqueChapterImageLinkList.add(current);
                }
            }
            if (!duplicatesToDelete.isEmpty()) {
                chapterImageLinkService.deleteAll(duplicatesToDelete);
            }


            String newContent = insertContentLinks(byIdAndIsDeletedFalse.getContent(), uniqueChapterImageLinkList);
            chapterCDO.setContent(newContent);
        }
        Long novelId = byIdAndIsDeletedFalse.getNovelId();
        int chapterNumber = byIdAndIsDeletedFalse.getChapterNumber();

        if (chapterNumber > 1) {
            Long preChapterID = chapterRepository.findIdByNovelIdAndChapterNumberAndIsDeletedFalse(novelId, chapterNumber - 1);
            chapterCDO.setPreId(preChapterID);
        }
        Long nextChapterID = chapterRepository.findIdByNovelIdAndChapterNumberAndIsDeletedFalse(novelId, chapterNumber + 1);
        chapterCDO.setNextId(nextChapterID);
        if (userId == null || userId < 0) {
            return chapterCDO;
        }
        readingRecordService.updateReadingRecord(userId, byIdAndIsDeletedFalse.getNovelId(), (long) byIdAndIsDeletedFalse.getChapterNumber(),byIdAndIsDeletedFalse.getId());
        return chapterCDO;
    }

    public String insertContentLinks(String content, List<ChapterImageLink> chapterImageLinks) {
        // 将字符串按行分割
        String[] lines = content.split("\n");
        List<String> resultLines = new ArrayList<>();

        // 按照位置插入 contentLink
        for (int i = 0; i < lines.length; i++) {
            resultLines.add(lines[i]);

            // 检查当前行是否需要插入 contentLink
            for (ChapterImageLink imageLink : chapterImageLinks) {
                String location = imageLink.getLocation();
                if (location != null && location.contains("_")) {
                    String[] parts = location.split("_");
                    int numerator = Integer.parseInt(parts[0]);
                    int denominator = Integer.parseInt(parts[1]);
                    int position = (int) Math.round((double) numerator / denominator * lines.length);

                    // 如果当前行是插入位置，插入 contentLink
                    if (i == position) {
                        resultLines.add(imageLink.getContentLink());
                    }
                }
            }
        }

        // 返回插入后的字符串
        return String.join("\n", resultLines);
    }

    public Chapter findChapterByChapterId(Long id) {
        return chapterRepository.findByIdAndIsDeletedFalse(id);
    }

    public Page<ChapterProjection> getChaptersByNovelIdWithPagination(Long novelId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chapterRepository.findAllByNovelIdAndIsDeletedFalse(novelId, pageable);
    }

    public List<ChapterProjection> getChaptersByNovelId(Long novelId) {
        return chapterRepository.findAllByNovelIdAndIsDeletedFalse(novelId);
    }

    public List<Long> findIdsByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(Long novelId) {
        return chapterRepository.findIdsByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(novelId);
    }

    public Integer findChapterNumberById(Long id){
        return chapterRepository.findChapterNumberById(id);
    }

}