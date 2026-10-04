package com.devstudy.vo;

import java.time.LocalDateTime;

public class FaqVO {

    private Long faqIdx;
    private String question;
    private String answer;
    private Integer sortOrder;
    private Integer isVisible;
    private Long memberIdx;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getFaqIdx() {
        return faqIdx;
    }

    public void setFaqIdx(Long faqIdx) {
        this.faqIdx = faqIdx;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Integer isVisible) {
        this.isVisible = isVisible;
    }

    public Long getMemberIdx() {
        return memberIdx;
    }

    public void setMemberIdx(Long memberIdx) {
        this.memberIdx = memberIdx;
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