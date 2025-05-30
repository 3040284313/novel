package com.wtl.novel.CDO;

import com.wtl.novel.entity.Chapter;

import java.util.Date;

public class ChapterCDO {

    public ChapterCDO(Chapter chapter) {
        this.id = chapter.getId();
        this.title = chapter.getTitle();
        this.content = chapter.getContent();
        this.novelId = chapter.getNovelId();
        this.chapterNumber = chapter.getChapterNumber();
        this.updatedAt = chapter.getUpdatedAt();
    }

    private Long id;

    private Long novelId; // 仅作为字段存储小说ID

    private boolean isDeleted = false;

    private String title; // 章节标题
    private int chapterNumber; // 章节编号
    private String content; // 章节内容
    private Long preId;
    private Long nextId;
    private Date updatedAt;

    public Long getPreId() {
        return preId;
    }

    public void setPreId(Long preId) {
        this.preId = preId;
    }

    public Long getNextId() {
        return nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }
}
