package com.family.parentalcontrol.models;

import java.io.Serializable;
import java.util.Date;

public class Location implements Serializable {
    private String id;
    private String childId;
    private double latitude;
    private double longitude;
    private float accuracy;
    private int batteryLevel;
    private String address;
    private Date timestamp;

    public Location() {
    }

    public Location(String childId, double latitude, double longitude, float accuracy) {
        this.childId = childId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
