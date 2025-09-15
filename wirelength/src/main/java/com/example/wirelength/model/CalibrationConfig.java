package com.example.wirelength.model;

import java.util.Date;

public class CalibrationConfig {
    private String name;
    private double pixelToMmRatio;
    private String cameraSetup;
    private String description;
    private boolean active;
    private Date createdAt;

    public CalibrationConfig() {
        this.createdAt = new Date();
        this.active = false;
    }

    public CalibrationConfig(String name, double pixelToMmRatio, String cameraSetup, String description) {
        this.name = name;
        this.pixelToMmRatio = pixelToMmRatio;
        this.cameraSetup = cameraSetup;
        this.description = description;
        this.active = false;
        this.createdAt = new Date();
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getPixelToMmRatio() {
        return pixelToMmRatio;
    }

    public String getCameraSetup() {
        return cameraSetup;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPixelToMmRatio(double pixelToMmRatio) {
        this.pixelToMmRatio = pixelToMmRatio;
    }

    public void setCameraSetup(String cameraSetup) {
        this.cameraSetup = cameraSetup;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CalibrationConfig{" +
                "name='" + name + '\'' +
                ", pixelToMmRatio=" + pixelToMmRatio +
                ", cameraSetup='" + cameraSetup + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}