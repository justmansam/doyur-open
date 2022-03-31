package com.neftisoft.doyur;

// IT IS FOR LOBBY ACTIVITY !!!

public class FoodLobby {

    private String avatarPhotoUrl;
    private String userName;
    private String msg;
    private String uid;
    private String currentTime;
    private String lobbyItemKey;

    public FoodLobby() {
    }

    public FoodLobby(String userName, String msg, String photoUrl2, String uid, String currentTime, String lobbyKey) {  //MUST BE EXACTLY THE SAME WITH THE DATABASE KEY VALUES! (NOT SURE)

        this.avatarPhotoUrl = photoUrl2;
        this.userName = userName;
        this.msg = msg;
        this.uid = uid;
        this.currentTime = currentTime;
        this.lobbyItemKey = lobbyKey;
    }

    public String getUid() {
        return uid;
    } //MUST BE SAME WITH THE DATABASE REFERENCE NAME (DB Ref uid = getUid)

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPhotoUrl2() {
        return avatarPhotoUrl;
    }

    public void setPhotoUrl2(String photoUrl2) {
        this.avatarPhotoUrl = photoUrl2;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getLobbyKey() {
        return lobbyItemKey;
    }

    public void setLobbyKey(String lobbyKey) {
        this.lobbyItemKey = lobbyKey;
    }
}
