package com.wtl.novel.CDO;


import com.wtl.novel.entity.Novel;

public class NovelCTO {
    private Long id;
    private String title;
    private String trueName;
    private String photoUrl;
    private String novelType;
    private Long up;
    private String trueId;
    private Long fontNumber;
    private String platform;
    private boolean isDeleted = false;
    private Long lastChapter;
    private Long lastChapterId;

    public NovelCTO(Novel novel, Long lastChapter, Long lastChapterId) {
        this.id = novel.getId();
        this.title = novel.getTitle();
        this.trueName = novel.getTrueName();
        this.photoUrl = novel.getPhotoUrl();
        this.novelType = novel.getNovelType();
        this.up = novel.getUp();
        this.trueId = novel.getTrueId();
        this.fontNumber = novel.getFontNumber();
        this.platform = novel.getPlatform();
        this.isDeleted = novel.isDeleted();
        this.lastChapter = lastChapter;
        this.lastChapterId = lastChapterId;
    }

    public Long getLastChapterId() {
        return lastChapterId;
    }

    public void setLastChapterId(Long lastChapterId) {
        this.lastChapterId = lastChapterId;
    }

    public Long getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(Long lastChapter) {
        this.lastChapter = lastChapter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrueName() {
        return trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getNovelType() {
        return novelType;
    }

    public void setNovelType(String novelType) {
        this.novelType = novelType;
    }

    public Long getUp() {
        return up;
    }

    public void setUp(Long up) {
        this.up = up;
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    public Long getFontNumber() {
        return fontNumber;
    }

    public void setFontNumber(Long fontNumber) {
        this.fontNumber = fontNumber;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
