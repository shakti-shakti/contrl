package com.family.parentalcontrol.models;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String id;
    private String deviceMode; // "parent" or "child"
    private String deviceName;
    private String masterPin;
    private Date createdAt;

    public User() {
    }

    public User(String id, String deviceMode, String deviceName, String masterPin) {
        this.id = id;
        this.deviceMode = deviceMode;
        this.deviceName = deviceName;
        this.masterPin = masterPin;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceMode() {
        return deviceMode;
    }

    public void setDeviceMode(String deviceMode) {
        this.deviceMode = deviceMode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMasterPin() {
        return masterPin;
    }

    public void setMasterPin(String masterPin) {
        this.masterPin = masterPin;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
