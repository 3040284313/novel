package com.wtl.novel.entity;


import java.time.LocalDateTime;
import java.util.Date;
import jakarta.persistence.*;

@Entity
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long novelId; // 仅作为字段存储小说ID
    private String trueId; // 仅作为字段存储小说ID
    private boolean ownPhoto;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1)")
    private boolean isDeleted = false;

    private String title; // 章节标题
    private int chapterNumber; // 章节编号
    @Column(columnDefinition = "mediumtext")
    private String content; // 章节内容
    // 添加 updated_at 字段
    @Column(name = "updated_at", nullable = false, updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    public boolean isOwnPhoto() {
        return ownPhoto;
    }

    public void setOwnPhoto(boolean ownPhoto) {
        this.ownPhoto = ownPhoto;
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

    // 使用 @PrePersist 自动设置 createdAt、isDelete 和 isResolved 字段
    @PrePersist
    protected void onCreate() {
        updatedAt = new Date();
    }
}