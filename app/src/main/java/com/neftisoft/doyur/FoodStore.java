package com.neftisoft.doyur;

// IT IS FOR STORE ACTIVITY (SAVING STORE) (ACCORDING TO THE LOCATION!!!)

public class FoodStore {
    private String userName;
    private String storeUid;
    private String storeName;
    private String storeInfo;
    private String storeKey;
    private String mainStoreImageUri;
    private String additionalStoreImageUri2;
    private String additionalStoreImageUri3;
    private String additionalStoreImageUri4;
    private String additionalStoreImageUri5;

    public FoodStore() {
    }

    public FoodStore(String uname, String ustoreUid, String ustoreName, String ustoreInfo, String ustoreKey, String umainStoreImageUri, String uadditionalStoreImageUri2, String uadditionalStoreImageUri3, String uadditionalStoreImageUri4, String uadditionalStoreImageUri5) {
        this.userName = uname;
        this.storeUid = ustoreUid;
        this.storeName = ustoreName;
        this.storeInfo = ustoreInfo;
        this.storeKey = ustoreKey;
        this.mainStoreImageUri = umainStoreImageUri;
        this.additionalStoreImageUri2 = uadditionalStoreImageUri2;
        this.additionalStoreImageUri3 = uadditionalStoreImageUri3;
        this.additionalStoreImageUri4 = uadditionalStoreImageUri4;
        this.additionalStoreImageUri5 = uadditionalStoreImageUri5;
    }

    public String getStoreUsername() {
        return userName;
    }

    public void setStoreUsername(String uname) {
        this.userName = uname;
    }

    public String getStoreUid() {
        return storeUid;
    }

    public void getStoreUid(String ustoreUid) {
        this.storeUid = ustoreUid;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String ustoreName) {
        this.storeName = ustoreName;
    }

    public String getStoreInfo() {
        return storeInfo;
    }

    public void setStoreInfo(String ustoreInfo) {
        this.storeInfo = ustoreInfo;
    }

    public String getStoreKey() {
        return storeKey;
    }

    public void setStoreKey(String ustoreKey) {
        this.storeKey = ustoreKey;
    }

    public String getMainStoreImageUri() {
        return mainStoreImageUri;
    }

    public void setMainStoreImageUri(String umainStoreImageUri) {
        this.mainStoreImageUri = umainStoreImageUri;
    }

    public String getAdditionalStoreImageUri2() {
        return additionalStoreImageUri2;
    }

    public void setAdditionalStoreImageUri2(String uadditionalStoreImageUri2) {
        this.additionalStoreImageUri2 = uadditionalStoreImageUri2;
    }

    public String getAdditionalStoreImageUri3() {
        return additionalStoreImageUri3;
    }

    public void setAdditionalStoreImageUri3(String uadditionalStoreImageUri3) {
        this.additionalStoreImageUri3 = uadditionalStoreImageUri3;
    }

    public String getAdditionalStoreImageUri4() {
        return additionalStoreImageUri4;
    }

    public void setAdditionalStoreImageUri4(String uadditionalStoreImageUri4) {
        this.additionalStoreImageUri4 = uadditionalStoreImageUri4;
    }

    public String getAdditionalStoreImageUri5() {
        return additionalStoreImageUri5;
    }

    public void setAdditionalStoreImageUri5(String uadditionalStoreImageUri5) {
        this.additionalStoreImageUri5 = uadditionalStoreImageUri5;
    }
}
