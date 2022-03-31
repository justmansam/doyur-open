package com.neftisoft.doyur;

public class FoodMessage {

    private String text;
    private String name;
    private String photoUrl;
    private String timeNow;
    private String uid3;
    private String key1;

    public FoodMessage() {
    }

    public FoodMessage(String text1, String name1, String photoUrl1, String timeNow1, String uid4, String key4) {
        this.text = text1;
        this.name = name1;
        this.photoUrl = photoUrl1;
        this.timeNow = timeNow1;
        this.uid3 = uid4;
        this.key1 = key4;
    }

    public String getText1() {
        return text;
    }

    public void setText1(String text1) {
        this.text = text1;
    }

    public String getName1() {
        return name;
    }

    public void setName1(String name1) {
        this.name = name1;
    }

    public String getPhotoUrl1() {
        return photoUrl;
    }

    public void setPhotoUrl1(String photoUrl1) {
        this.photoUrl = photoUrl1;
    }

    public String getTimeNow1() {
        return timeNow;
    }

    public void setTimeNow1(String timeNow1) {
        this.timeNow = timeNow1;
    }

    public String getUid3() {
        return uid3;
    }

    public void setUid3(String uid4) {
        this.uid3 = uid4;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key4) {
        this.key1 = key4;
    }
}
