package com.mpos.db;

/**
 * Created by chenld on 2016/12/19.
 */
public class MPos {

    private String mac;//设备地址，主键
    private String name;//设备名
    private String sn;//sn号
    private String pn;//pn号
    private String os_version;//os版本
    private String boot_version;//boot版本
    private String battery;//电量
    private String isupdate;//10 更新，其他不更新



    public MPos() {
    }

//    public MPos(String isupdate){
//        this.isupdate = isupdate;
//    }

    public MPos(String mac, String name) {
        this.mac = mac;
        this.name = name;
    }

//    public MPos(String mac, String name, String isupdate){
//        this.mac = mac;
//        this.name = name;
//        this.isupdate = isupdate;
//    }

    public MPos(String mac, String name, String sn, String pn, String os_version, String boot_version,
                String battery) {
        this.mac = mac;
        this.name = name;
        this.sn = sn;
        this.pn = pn;
        this.os_version = os_version;
        this.boot_version = boot_version;
        this.battery = battery;
    }


    public MPos(String mac, String name, String sn, String pn, String os_version, String boot_version,
                String battery, String isupdate) {
        this.mac = mac;
        this.name = name;
        this.sn = sn;
        this.pn = pn;
        this.os_version = os_version;
        this.boot_version = boot_version;
        this.battery = battery;
        this.isupdate = isupdate;
    }

    public String getIsupdate() {
        return isupdate;
    }

    public void setIsupdate(String isupdate) {
        this.isupdate = isupdate;
    }
    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getBoot_version() {

        return boot_version;
    }

    public void setBoot_version(String boot_version) {
        this.boot_version = boot_version;
    }

    public String getOs_version() {

        return os_version;
    }

    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    public String getPn() {

        return pn;
    }

    public void setPn(String pn) {
        this.pn = pn;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSn() {

        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getMac() {

        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "MPos{" +
                "mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                ", sn='" + sn + '\'' +
                ", pn='" + pn + '\'' +
                ", os_version='" + os_version + '\'' +
                ", boot_version='" + boot_version + '\'' +
                ", battery='" + battery + '\'' +
                ", isupdate=" + isupdate +
                '}';
    }
}
