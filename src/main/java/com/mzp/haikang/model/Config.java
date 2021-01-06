package com.mzp.haikang.model;

/**
 * @
 * @author  mzp
 * @date  2021/1/6 15:19
 * @version 1.0
 * 
 */
public class Config {
    private String ip;

    private String hikusername;

    private String hikpassword;

    private String hikport;

    private String deviceid;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHikusername() {
        return hikusername;
    }

    public void setHikusername(String hikusername) {
        this.hikusername = hikusername;
    }

    public String getHikpassword() {
        return hikpassword;
    }

    public void setHikpassword(String hikpassword) {
        this.hikpassword = hikpassword;
    }

    public String getHikport() {
        return hikport;
    }

    public void setHikport(String hikport) {
        this.hikport = hikport;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }
}