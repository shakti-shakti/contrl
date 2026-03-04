package com.family.parentalcontrol.models;

import java.io.Serializable;
import java.util.Date;

public class Child implements Serializable {
    private String id;
    private String parentId;
    private String childName;
    private int childAge;
    private String deviceName;
    private String status; // "online", "offline"
    private Date lastSeen;
    private int batteryLevel;
    private String currentLocation;
    private Date pairedAt;
    private boolean isMonitored;

    public Child() {
    }

    public Child(String id, String parentId, String childName, int childAge) {
        this.id = id;
        this.parentId = parentId;
        this.childName = childName;
        this.childAge = childAge;
        this.status = "offline";
        this.batteryLevel = 100;
        this.pairedAt = new Date();
        this.isMonitored = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public int getChildAge() {
        return childAge;
    }

    public void setChildAge(int childAge) {
        this.childAge = childAge;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Date getPairedAt() {
        return pairedAt;
    }

    public void setPairedAt(Date pairedAt) {
        this.pairedAt = pairedAt;
    }

    public boolean isMonitored() {
        return isMonitored;
    }

    public void setMonitored(boolean monitored) {
        isMonitored = monitored;
    }

    // Convenience methods
    public void setName(String name) {
        this.childName = name;
    }

    public String getName() {
        return this.childName;
    }

    public void setAge(int age) {
        this.childAge = age;
    }

    public int getAge() {
        return this.childAge;
    }
}
