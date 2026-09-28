package com.devstudy.vo;

import java.time.LocalDateTime;

public class NoticeVO {

    private Long noticeIdx;
    private Long memberIdx;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer isPinned;
    private Integer isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getNoticeIdx() {
        return noticeIdx;
    }

    public void setNoticeIdx(Long noticeIdx) {
        this.noticeIdx = noticeIdx;
    }

    public Long getMemberIdx() {
        return memberIdx;
    }

    public void setMemberIdx(Long memberIdx) {
        this.memberIdx = memberIdx;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    public Integer getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Integer isVisible) {
        this.isVisible = isVisible;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}