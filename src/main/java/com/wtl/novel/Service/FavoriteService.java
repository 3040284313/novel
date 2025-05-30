package com.wtl.novel.Service;

import com.wtl.novel.CDO.FavoriteCTO;
import com.wtl.novel.entity.Favorite;
import java.util.List;

import com.wtl.novel.entity.ReadingRecord;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.FavoriteRepository;
import com.wtl.novel.repository.ReadingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService{

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ReadingRecordRepository readingRecordRepository;

    public List<Favorite> getAllFavoritesByUserId(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }

    public boolean existsByUserIdAndObjectIdAndFavoriteType(Long userId, Long objectId, String favoriteType) {
        return favoriteRepository.existsByUserIdAndObjectIdAndFavoriteType(userId, objectId, favoriteType);
    }

    public List<FavoriteCTO> getFavoritesByUserIdAndGroup(Long groupId, User favoriteUser) {
        List<Favorite> byUserIdAndFavoriteType = favoriteRepository.findByUserIdAndGroupId(favoriteUser.getId(), groupId);
        List<Long> objectIdList = byUserIdAndFavoriteType.stream().map(Favorite::getObjectId).toList();
        List<ReadingRecord> byUserIdAndNovelIdIn = readingRecordRepository.findByUserIdAndNovelIdIn(favoriteUser.getId(), objectIdList);
        Map<Long, ReadingRecord> readingRecordMap = byUserIdAndNovelIdIn.stream()
                .collect(Collectors.toMap(
                        ReadingRecord::getNovelId,
                        record -> record
                ));

        // 遍历 Favorite 列表，构造 FavoriteCTO
        return byUserIdAndFavoriteType.stream()
                .map(favorite -> {
                    // 获取对应的 ReadingRecord
                    ReadingRecord readingRecord = readingRecordMap.get(favorite.getObjectId());

                    // 如果没有对应的 ReadingRecord，则 lastChapter 为 null 或默认值
                    Long lastChapter = (readingRecord != null) ? readingRecord.getLastChapter() : null;

                    // 构造 FavoriteCTO
                    return new FavoriteCTO(
                            favorite,
                            lastChapter
                    );
                })
                .collect(Collectors.toList());
    }

    public List<FavoriteCTO> getFavoritesByUserIdAndType(String favoriteType, User favoriteUser) {
        List<Favorite> byUserIdAndFavoriteType = favoriteRepository.findByUserIdAndFavoriteType(favoriteUser.getId(), favoriteType);
        List<Long> objectIdList = byUserIdAndFavoriteType.stream().map(Favorite::getObjectId).toList();
        List<ReadingRecord> byUserIdAndNovelIdIn = readingRecordRepository.findByUserIdAndNovelIdIn(favoriteUser.getId(), objectIdList);
        Map<Long, ReadingRecord> readingRecordMap = byUserIdAndNovelIdIn.stream()
                .collect(Collectors.toMap(
                        ReadingRecord::getNovelId,
                        record -> record
                ));

        // 遍历 Favorite 列表，构造 FavoriteCTO
        return byUserIdAndFavoriteType.stream()
                .map(favorite -> {
                    // 获取对应的 ReadingRecord
                    ReadingRecord readingRecord = readingRecordMap.get(favorite.getObjectId());

                    // 如果没有对应的 ReadingRecord，则 lastChapter 为 null 或默认值
                    Long lastChapter = (readingRecord != null) ? readingRecord.getLastChapter() : null;

                    // 构造 FavoriteCTO
                    return new FavoriteCTO(
                            favorite,
                            lastChapter
                    );
                })
                .collect(Collectors.toList());
    }

}