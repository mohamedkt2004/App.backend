package com.example.wirelength.model;

import java.util.Date;

public class AnalysisResult {
    private boolean success;
    private String message;
    private Double lengthInMm;
    private Double lengthInCm;
    private Double lengthInM;
    private Double lengthInPixels;
    private String calibrationUsed;
    private Double pixelRatio;
    private Date analysisDate;
    private String imageName;
    private Integer imageWidth;
    private Integer imageHeight;
    private String analysisType; // "bar_reference", "calibration", "fabric_measurement"

    public AnalysisResult() {
        this.analysisDate = new Date();
    }

    // Constructeur complet
    public AnalysisResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.analysisDate = new Date();
    }

    // Getters et Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getLengthInMm() {
        return lengthInMm;
    }

    public void setLengthInMm(Double lengthInMm) {
        this.lengthInMm = lengthInMm;
    }

    public Double getLengthInCm() {
        return lengthInCm;
    }

    public void setLengthInCm(Double lengthInCm) {
        this.lengthInCm = lengthInCm;
    }

    public Double getLengthInM() {
        return lengthInM;
    }

    public void setLengthInM(Double lengthInM) {
        this.lengthInM = lengthInM;
    }

    public Double getLengthInPixels() {
        return lengthInPixels;
    }

    public void setLengthInPixels(Double lengthInPixels) {
        this.lengthInPixels = lengthInPixels;
    }

    public String getCalibrationUsed() {
        return calibrationUsed;
    }

    public void setCalibrationUsed(String calibrationUsed) {
        this.calibrationUsed = calibrationUsed;
    }

    public Double getPixelRatio() {
        return pixelRatio;
    }

    public void setPixelRatio(Double pixelRatio) {
        this.pixelRatio = pixelRatio;
    }

    public Date getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(Date analysisDate) {
        this.analysisDate = analysisDate;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", lengthInMm=" + lengthInMm +
                ", lengthInCm=" + lengthInCm +
                ", lengthInM=" + lengthInM +
                ", calibrationUsed='" + calibrationUsed + '\'' +
                ", analysisDate=" + analysisDate +
                ", analysisType='" + analysisType + '\'' +
                '}';
    }
}