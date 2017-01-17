package com.mpos;

/**
 * Created by chenld on 2017/1/3.
 */

public class UpdateModel {
    private String btDevName;
    private String btDevMac;
    private int    progress;

    public UpdateModel(String btDevName, String btDevMac, int progress) {
        super();
        this.btDevName = btDevName;
        this.btDevMac = btDevMac;
        this.progress = progress;
    }

    public UpdateModel() {
    }

    public String getBtDevName() {
        return btDevName;
    }
    public void setBtDevName(String btDevName) {
        this.btDevName = btDevName;
    }
    public String getBtDevMac() {
        return btDevMac;
    }
    public void setBtDevMac(String btDevMac) {
        this.btDevMac = btDevMac;
    }
    public int getProgress() {
        return progress;
    }
    public void setProgress(int progress) {
        this.progress = progress;
    }
}
