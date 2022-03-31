package com.neftisoft.doyur;

// IT IS FOR FOOD DESIRE FRAGMENT !!!

public class FoodDesired {

    private String desiredAvatarPhoto;
    private String desiredUserName;
    private String desiredFoodName;
    private String desiredUserId;
    private String desiredItemKey;

    public FoodDesired() {
    }

    public FoodDesired(String avatar, String desire, String key, String name, String uid) { //MUST BE EXACTLY THE SAME WITH THE DATABASE KEY VALUES! (NOT SURE)

        this.desiredAvatarPhoto = avatar;
        this.desiredUserName = name;
        this.desiredFoodName = desire;
        this.desiredUserId = uid;
        this.desiredItemKey = key;
    }

    public String getUid() {
        return desiredUserId;
    }  //MUST BE SAME WITH THE DATABASE REFERENCE NAME (DB Ref uid = getUid)

    public void setUid(String uid) {
        this.desiredUserId = uid;
    }

    public String getName() {
        return desiredUserName;
    }

    public void setName(String name) {
        this.desiredUserName = name;
    }

    public String getDesire() {
        return desiredFoodName;
    }

    public void setDesire(String desire) {
        this.desiredFoodName = desire;
    }

    public String getAvatar() {
        return desiredAvatarPhoto;
    }

    public void setAvatar(String avatar) {
        this.desiredAvatarPhoto = avatar;
    }

    public String getKey() {
        return desiredItemKey;
    }

    public void setKey(String key) {
        this.desiredItemKey = key;
    }
}
