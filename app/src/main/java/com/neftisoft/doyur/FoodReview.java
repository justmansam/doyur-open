package com.neftisoft.doyur;

// IT IS FOR REVIEW ACTIVITY !!!

public class FoodReview {

    private String revAvatar;
    private String revName;
    private String revRate;
    private String rev;
    private String revDate;
    private String revUid;
    private String revKey;

    public FoodReview() {
    }

    public FoodReview(String reviewerAvatar, String reviewerName, String reviewerRate, String review, String dateNow, String reviewerUid, String reviewKey) {

        this.revAvatar = reviewerAvatar;
        this.revName = reviewerName;
        this.revRate = reviewerRate;
        this.rev = review;
        this.revDate = dateNow;
        this.revUid = reviewerUid;
        this.revKey = reviewKey;
    }

    public String getReviewerAvatar() {
        return revAvatar;
    } //MUST BE SAME WITH THE DATABASE REFERENCE NAME (DB Ref reviewerAvatar = getReviewerAvatar)

    public void setReviewerAvatar(String reviewerAvatar) {
        this.revAvatar = reviewerAvatar;
    }

    public String getReviewerName() {
        return revName;
    }

    public void setReviewerName(String reviewerName) {
        this.revName = reviewerName;
    }

    public String getReviewerRate() {
        return revRate;
    }

    public void setReviewerRate(String reviewerRate) {
        this.revRate = reviewerRate;
    }

    public String getReview() {
        return rev;
    }

    public void setReview(String review) {
        this.rev = review;
    }

    public String getDateNow() {
        return revDate;
    }

    public void setDateNow(String dateNow) {
        this.revDate = dateNow;
    }

    public String getReviewerUid() {
        return revUid;
    }

    public void setReviewerUid(String reviewerUid) {
        this.revUid = reviewerUid;
    }

    public String getReviewKey() {
        return revKey;
    }

    public void setReviewKey(String reviewKey) {
        this.revKey = reviewKey;
    }
}
