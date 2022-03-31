package com.neftisoft.doyur;

// IT IS FOR MAIN SCREEN(PARTY) ACTIVITY (PICKING OR SHOOTING PHOTO AND SHOWING IT IN THE MAIN SCREEN) (ACCORDING TO THE LOCATION!!!)

public class FoodParty {
    private String partyUid;
    private String name;
    private String photoUrl;
    private String key;
    private String price;

    public FoodParty() {
    }

    public FoodParty(String name, String photoUrl, String uid, String foodKey, String foodPrice) {
        this.partyUid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
        this.key = foodKey;
        this.price = foodPrice;
    }

    public String getUid() {
        return partyUid;
    }

    public void setUid(String uid) {
        this.partyUid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String foodKey) {
        this.key = foodKey;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String foodPrice) {
        this.price = foodPrice;
    }
}
