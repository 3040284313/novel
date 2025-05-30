package com.wtl.novel.CDO;

public class NovelSearchRequest {
    private String platform;
    private String fontNumber;
    private String tabIds; // 逗号分隔的标签ID字符串
    private int page;
    private int size;

    // Getters and Setters
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getFontNumber() {
        return fontNumber;
    }

    public void setFontNumber(String fontNumber) {
        this.fontNumber = fontNumber;
    }

    public String getTabIds() {
        return tabIds;
    }

    public void setTabIds(String tabIds) {
        this.tabIds = tabIds;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}