package com.family.parentalcontrol.models;

import java.io.Serializable;
import java.util.Date;

public class AppUsage implements Serializable {
    private String id;
    private String childId;
    private String packageName;
    private String appName;
    private long usageDuration; // in milliseconds
    private Date timestamp;
    private String category; // "game", "social", "education", etc.

    public AppUsage() {
    }

    public AppUsage(String childId, String packageName, String appName, long usageDuration) {
        this.childId = childId;
        this.packageName = packageName;
        this.appName = appName;
        this.usageDuration = usageDuration;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getUsageDuration() {
        return usageDuration;
    }

    public void setUsageDuration(long usageDuration) {
        this.usageDuration = usageDuration;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
