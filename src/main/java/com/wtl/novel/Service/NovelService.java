package com.wtl.novel.Service;

import com.wtl.novel.CDO.NovelCTO;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NovelService {
    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private NovelTagRepository novelTagRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private ReadingRecordRepository readingRecordRepository;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private ReadingRecordService readingRecordService;

    public int incrementFontNumberById(Long id, Long increment) {
        return novelRepository.incrementFontNumberById(id, increment);
    }

    public Page<Novel> getNovelsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return novelRepository.findAllNotDeletedWithPageable(pageable);
    }

    // 根据标签查小说
    public Page<Novel> getNovelsByTagIdWithPagination(Long tagId, Pageable pageable) {
        Page<NovelTag> byId = novelTagRepository.findById(tagId, pageable);
        List<Long> novelIdList = byId.map(NovelTag::getNovelId).toList();
        return new PageImpl<>(novelRepository.findAllById(novelIdList), byId.getPageable(), byId.getTotalElements());
    }

    public List<Tag> getTagsByNovelId( Long novelId) {
        List<NovelTag> byNovelId = novelTagRepository.findByNovelId(novelId);
        return tagRepository.findByIdIn(byNovelId.stream().map(NovelTag::getTagId).toList());
    }

    // 分页查询小说
//    public Page<Novel> getNovelsWithPagination(Long tagId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        if (tagId == 0) {
//            return novelRepository.findAllByIsDeletedFalseOrderByUpDesc(pageable);
//        } else if (tagId == 4) {
//            List<Long> tags = new ArrayList<>();
//            tags.add(1L);
//            List<Long> novelIdList = novelTagRepository.findDistinctNovelIdByTagIdNotIn(tags);
//            return novelRepository.findByIdInAndIsDeletedFalseOrderByUpDesc(novelIdList, pageable);
//        } else {
//            List<NovelTag> byTagId = novelTagRepository.findByTagId(tagId);
//            List<Long> novelIdList = byTagId.stream().map(NovelTag::getNovelId).toList();
//            return novelRepository.findByIdInAndIsDeletedFalseOrderByUpDesc(novelIdList, pageable);
//        }
//    }

    public Page<Novel> findByTitleContainingOrTrueNameContaining(String keyword) {
        Pageable pageable = PageRequest.of(0, 10);
        return novelRepository.findByTitleContainingAndIsDeletedFalseOrTrueNameContainingAndIsDeletedFalseOrderByUpDesc(keyword, keyword, pageable);
    }

    // 获取特定小说的信息
    public NovelCTO findNovelById(Long id, HttpServletRequest httpRequest) {
        Novel byIdAndIsDeletedFalse = novelRepository.findByIdAndIsDeletedFalse(id);
        try {
            String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
            String authorizationHeader = authorizationInfo[0];
            Credential credential = credentialService.findByToken(authorizationHeader);
            ReadingRecord byUserIdAndNovelId = readingRecordRepository.findByUserIdAndNovelId(credential.getUser().getId(), id);
            return new NovelCTO(byIdAndIsDeletedFalse, byUserIdAndNovelId.getLastChapter(), byUserIdAndNovelId.getLastChapterId());
        } catch (Exception e) {
            return new NovelCTO(byIdAndIsDeletedFalse, null, null);
        }
    }

    // 收藏功能
    @Transactional
    public Integer increaseUp(Long id, User user, String type, String favoriteType, Long groupId) {
        // 查询小说是否存在且未被删除
        Novel novel = novelRepository.findByIdAndIsDeletedFalse(id);
        if (novel == null) {
            return 0; // 小说不存在或已被删除，返回0
        }

        List<Favorite> favoriteNovel = favoriteRepository.findByUserIdAndObjectIdAndFavoriteType(user.getId(), novel.getId(), favoriteType);

        // 如果用户已经收藏过且当前操作是收藏，则不做任何处理
        if (!favoriteNovel.isEmpty() && type.equals("up")) {
            return 0;
        }
        // 如果用户未收藏过且当前操作是取消收藏，则不做任何处理
        else if (favoriteNovel.isEmpty() && type.equals("down")) {
            return 0;
        }

        // 根据操作类型处理收藏或取消收藏
        if (type.equals("up")) { // 收藏
            novel.setUp(novel.getUp() + 1); // 增加收藏数
            novelRepository.save(novel); // 保存小说信息

            // 创建收藏记录
            Favorite favorite = new Favorite(user.getId(), favoriteType, novel.getId(), novel.getTitle());
            favorite.setGroupId(groupId);
            favoriteRepository.save(favorite);

            return 1; // 返回1表示收藏成功
        } else if (type.equals("down")) { // 取消收藏
            novel.setUp(novel.getUp() - 1); // 减少收藏数
            novelRepository.save(novel); // 保存小说信息

            // 删除收藏记录
            favoriteRepository.deleteByUserIdAndObjectIdAndFavoriteType(user.getId(), novel.getId(), favoriteType);
            return -1; // 返回-1表示取消收藏成功
        }
        return 0; // 其他情况返回0
    }


//    ===
    // 分页查询小说
//    public Page<Novel> getNovelsWithPaginationByArgs(String platform, String novelType,String fontNumber, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        String[] parts = fontNumber.split("_");
//
//        // 检查是否分隔成功且有两部分
//        if (parts.length != 2) {
//            throw new IllegalArgumentException("Invalid input format. Expected format: 'number_number'");
//        }
//
//        // 将字符串转换为 Long
//        Long firstNumber = Long.parseLong(parts[0]);
//        Long secondNumber = Long.parseLong(parts[1]);
//        if (novelType.equals("全部")) {
//            return novelRepository.findByPlatformAndFontNumberBetweenAndIsDeletedFalseOrderByUpDesc(platform,firstNumber,secondNumber,pageable);
//        }
//
//        return novelRepository.findByPlatformAndNovelTypeAndFontNumberBetweenAndIsDeletedFalseOrderByUpDesc(platform, novelType,firstNumber,secondNumber,pageable);
//    }


    public Page<NovelCTO> getNovelsWithPagination(String platform, String fontNumber,String tagIdStr, Pageable pageable, Long userId) {
        List<Long> tagIdList = convertToLongList(tagIdStr);
        String[] parts = fontNumber.split("_");
        // 检查是否分隔成功且有两部分
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid input format. Expected format: 'number_number'");
        }
        // 将字符串转换为 Long
        Long firstNumber = Long.parseLong(parts[0]);
        Long secondNumber = Long.parseLong(parts[1]);
        if (tagIdList.contains(0L)) {
            Page<NovelCTO> page = novelRepository.findByNovelCTOPlatformAndFontNumberBetweenAndIsDeletedFalseOrderByUpDesc(platform, firstNumber, secondNumber, pageable);
            if (userId != null) {
                List<ReadingRecord> records = readingRecordService.getReadingRecordsByBookIds(userId, page.getContent().stream().map(NovelCTO::getId).toList());
                populateReadingRecordInfo(page.getContent(), records);
            }
            return page;
        }  else {
            List<Long> novelIdList = novelTagRepository.findNovelIdsByAllTagIds(tagIdList, tagIdList.size());
            Page<NovelCTO> page = novelRepository.findNovelCTOByNovelIdsAndPlatformAndFontNumberRange(novelIdList, platform, firstNumber, secondNumber, pageable);
            if (userId != null) {
                List<ReadingRecord> records = readingRecordService.getReadingRecordsByBookIds(userId, page.getContent().stream().map(NovelCTO::getId).toList());
                populateReadingRecordInfo(page.getContent(), records);
            }
            return page;
        }
    }

    public void populateReadingRecordInfo(List<NovelCTO> novelCTOs, List<ReadingRecord> readingRecords) {
        // 创建一个Map，用于按小说ID快速查找对应的阅读记录
        Map<Long, ReadingRecord> readingRecordMap = new HashMap<>();
        for (ReadingRecord record : readingRecords) {
            readingRecordMap.put(record.getNovelId(), record);
        }

        // 遍历小说列表，填充阅读记录信息
        for (NovelCTO novelCTO : novelCTOs) {
            Long novelId = novelCTO.getId();
            ReadingRecord record = readingRecordMap.get(novelId);
            if (record != null) {
                novelCTO.setLastChapter(record.getLastChapter());
                novelCTO.setLastChapterId(record.getLastChapterId());
            }
        }
    }

    // 将逗号分隔的字符串转换为Long列表，并去重
    public static List<Long> convertToLongList(String ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return Stream.of(ids.split(","))
                .map(id -> id.trim().isEmpty() ? null : Long.parseLong(id))
                .filter(Objects::nonNull)
                .distinct() // 去重
                .collect(Collectors.toList());
    }
}