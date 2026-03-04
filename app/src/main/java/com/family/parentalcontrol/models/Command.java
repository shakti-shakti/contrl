package com.family.parentalcontrol.models;

import java.io.Serializable;
import java.util.Date;

public class Command implements Serializable {
    private String id;
    private String parentId;
    private String childId;
    private String command;
    private String parameters; // JSON string
    private String status; // pending, executed
    private Date createdAt;
    private Date executedAt;

    public Command() {
    }

    public Command(String parentId, String childId, String command, String parameters) {
        this.parentId = parentId;
        this.childId = childId;
        this.command = command;
        this.parameters = parameters;
        this.status = "pending";
        this.createdAt = new Date();
    }

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

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Date executedAt) {
        this.executedAt = executedAt;
    }
}
